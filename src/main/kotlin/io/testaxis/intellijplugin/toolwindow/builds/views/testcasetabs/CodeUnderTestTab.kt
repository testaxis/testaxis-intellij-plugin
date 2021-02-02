package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.ui.CollectionListModel
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBList
import com.intellij.ui.components.Label
import com.intellij.util.ui.components.BorderLayoutPanel
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.services.GitService
import io.testaxis.intellijplugin.toolwindow.borderLayoutPanel
import io.testaxis.intellijplugin.toolwindow.builds.NoPreviousBuildWarning
import io.testaxis.intellijplugin.toolwindow.builds.NotMatchingRevisionsWarning
import io.testaxis.intellijplugin.toolwindow.builds.views.BuildsUpdateHandler
import io.testaxis.intellijplugin.toolwindow.horizontal
import io.testaxis.intellijplugin.vcs.CoveredFile
import io.testaxis.intellijplugin.vcs.TextualDiff
import io.testaxis.intellijplugin.vcs.coveredFiles
import io.testaxis.intellijplugin.vcs.deletions
import io.testaxis.intellijplugin.vcs.textualDiff
import java.awt.Color
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import javax.swing.border.EmptyBorder

private const val SPLITTER_PROPORTION_ONE_THIRD = .33f
private const val SCROLL_WINDOW_START = -5

class CodeUnderTestTab(val project: Project) : TestCaseTab, BuildsUpdateHandler {
    override val tabName = "Code Under Test"

    private val coveredFilesList = JBList(CollectionListModel<CoveredFile>()).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        cellRenderer = CoveredFileCellRenderer()
    }

    private val panel = BorderLayoutPanel()
    private val editor = TestCodeEditorField(project)

    private val diffInformationLabel = JLabel()
    private val showFullDiffButton = JButton("Show Full Diff").apply { background = background.darker() }
    private val diffInformationPanel = borderLayoutPanel {
        addToLeft(
            horizontal(
                legendLabel("Covered and Changed", TestCodeEditorField.COVERED_AND_CHANGED_LINE_COLOR),
                legendLabel("Changed", TestCodeEditorField.CHANGED_LINE_COLOR),
                legendLabel("Covered", TestCodeEditorField.COVERED_LINE_COLOR),
            ).apply { background = background.darker() }
        )
        addToRight(
            horizontal(
                diffInformationLabel,
                showFullDiffButton
            ).apply { background = background.darker() }
        )
    }.apply {
        isVisible = false
        border = EmptyBorder(10, 10, 10, 10)
        background = background.darker()
    }

    private var buildHistory: List<Build> = emptyList()

    init {
        coveredFilesList.addListSelectionListener { showCoveredFile(coveredFilesList.selectedValue) }
    }

    private fun showCoveredFile(coveredFile: CoveredFile?) {
        diffInformationPanel.isVisible = false
        diffInformationLabel.text = ""

        coveredFile ?: return

        editor.showFile(coveredFile.getFile(project))
        editor.setCaretPosition(0)

        coveredFile.lines.forEach { editor.highlightCoveredLine(it) }

        coveredFile.vcsChange?.let { change ->
            if (change.type == Change.Type.MODIFICATION) {
                diffInformationPanel.isVisible = true
                showFullDiffButton.replaceActionListener { project.service<GitService>().showDiff(change) }

                val changeList = change.textualDiff()
                changeList.changedAndCoveredLines(coveredFile.lines).run {
                    forEach(editor::highlightCoveredAndChangedLine)
                    editor.moveCaretToLine(maxOf((firstOrNull() ?: 0) + SCROLL_WINDOW_START, 0))
                }
                changeList.changedLines().forEach(editor::highlightChangedLine)
                changeList.giveOptionalDeletionsWarning()
            }
        }

        editor.scrollToCaretPosition()
    }

    override fun activate() {
        coveredFilesList.selectedIndex = 0
        showCoveredFile(coveredFilesList.selectedValue)
    }

    override fun getComponent(): JComponent = panel.apply {
        add(
            OnePixelSplitter(SPLITTER_PROPORTION_ONE_THIRD).apply {
                firstComponent = BorderLayoutPanel().apply {
                    add(JScrollPane(coveredFilesList))
                }

                secondComponent = BorderLayoutPanel().apply {
                    add(editor)
                    addToBottom(diffInformationPanel)
                }
            }
        )
    }

    override fun setTestCaseExecution(testCaseExecution: TestCaseExecution) {
        editor.showText("")

        coveredFilesList.setPaintBusy(true)
        runBackgroundableTask("Collecting covered files", project, cancellable = false) {
            when (val coveredFiles = testCaseExecution.coveredFiles(project, buildHistory)) {
                null -> runInEdt { panel.addToTop(NoPreviousBuildWarning(project)) }
                else -> {
                    coveredFilesList.model = CollectionListModel(coveredFiles)
                    coveredFilesList.setPaintBusy(false)
                }
            }
        }

        panel.components.filterIsInstance<NotMatchingRevisionsWarning>().forEach { panel.remove(it) }
        NotMatchingRevisionsWarning(project, testCaseExecution.build).let {
            if (it.shouldBeApplied()) {
                panel.addToTop(it)
            }
        }
    }

    override fun handleNewBuilds(buildHistory: List<Build>) {
        this.buildHistory = buildHistory
    }

    private fun TextualDiff.changedLines() = this
        .map { fragment -> ((fragment.startLine2 + 1) until (fragment.endLine2 + 1)) }
        .flatMap { range -> range.toList() }

    private fun TextualDiff.changedAndCoveredLines(coveredLines: List<Int>) =
        changedLines().filter { coveredLines.contains(it) }

    private fun TextualDiff.highlightChanges(coveredLines: List<Int>) {
        onEach { fragment ->
            ((fragment.startLine2 + 1) until (fragment.endLine2 + 1)).forEach { lineNumber ->
                if (coveredLines.contains(lineNumber)) {
                    editor.highlightCoveredAndChangedLine(lineNumber)
                } else {
                    editor.highlightChangedLine(lineNumber)
                }
            }
        }
    }

    private fun TextualDiff.giveOptionalDeletionsWarning() = deletions().let {
        if (it == 1) {
            diffInformationLabel.text = "$it code fragment was removed, see the full diff."
        }
        if (it > 1) {
            diffInformationLabel.text = "$it code fragments were removed, see the full diff."
        }
    }

    private fun legendLabel(title: String, color: Color) =
        borderLayoutPanel {
            add(Label(title))
            background = color
            border = EmptyBorder(5, 5, 5, 5)
        }
}

fun JButton.replaceActionListener(actionListener: ActionListener) {
    actionListeners.forEach { removeActionListener(it) }
    addActionListener(actionListener)
}
