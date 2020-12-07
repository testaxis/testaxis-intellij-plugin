package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.ui.CollectionListModel
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.panel
import com.intellij.util.ui.components.BorderLayoutPanel
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.services.GitService
import io.testaxis.intellijplugin.services.PsiService
import io.testaxis.intellijplugin.toolwindow.builds.NoPreviousBuildWarning
import io.testaxis.intellijplugin.toolwindow.builds.NotMatchingRevisionsWarning
import io.testaxis.intellijplugin.toolwindow.builds.views.BuildsUpdateHandler
import io.testaxis.intellijplugin.vcs.TextualDiff
import io.testaxis.intellijplugin.vcs.changes
import io.testaxis.intellijplugin.vcs.deletions
import io.testaxis.intellijplugin.vcs.findPreviousBuild
import io.testaxis.intellijplugin.vcs.textualDiff
import kotlinx.coroutines.runBlocking
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.ListSelectionModel

private const val SPLITTER_PROPORTION_ONE_THIRD = .33f

class CodeUnderTestTab(val project: Project) : TestCaseTab, BuildsUpdateHandler {
    override val tabName = "Code Under Test"

    private val coveredFilesList = JBList(CollectionListModel<CoveredFile>()).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        cellRenderer = CoveredFileCellRenderer()
    }

    private val panel = BorderLayoutPanel()
    private val editor = TestCodeEditorField(project)

    private val diffInformationLabel = JLabel()
    private val showFullDiffButton = JButton("Show Full Diff")
    private val diffInformationPanel = panel {
        row {
            diffInformationLabel()
            showFullDiffButton()
        }
    }.apply { isVisible = false }

    private var buildHistory: List<Build> = emptyList()

    init {
        coveredFilesList.addListSelectionListener {
            diffInformationPanel.isVisible = false
            diffInformationLabel.text = ""

            if (coveredFilesList.selectedValue != null) {
                editor.showFile(coveredFilesList.selectedValue.getFile(project))
                coveredFilesList.selectedValue.lines.forEach { editor.highlightLine(it) }

                coveredFilesList.selectedValue.vcsChange?.let { change ->
                    println("Change: ${coveredFilesList.selectedValue.vcsChange}")
                    if (change.type == Change.Type.MODIFICATION) {
                        println("Change diff: ${coveredFilesList.selectedValue.vcsChange?.textualDiff()}")

                        diffInformationPanel.isVisible = true
                        showFullDiffButton.replaceActionListener { project.service<GitService>().showDiff(change) }

                        change.textualDiff().run {
                            highlightChanges()
                            giveOptionalDeletionsWarning()
                        }
                    }
                }
            }
        }
    }

    override fun activate() {
        coveredFilesList.selectedIndex = 0
    }

    override fun getComponent(): JComponent = panel.apply {
        add(
            OnePixelSplitter(SPLITTER_PROPORTION_ONE_THIRD).apply {
                firstComponent = BorderLayoutPanel().apply {
                    add(coveredFilesList)
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

        runBackgroundableTask("Collecting covered files", project, cancellable = false) {
            val model = CollectionListModel<CoveredFile>()

            val previousBuild = testCaseExecution.build?.findPreviousBuild(project, buildHistory)

            if (previousBuild == null) {
                invokeLater { panel.addToTop(NoPreviousBuildWarning(project)) }
            }

            val changes = previousBuild?.let { testCaseExecution.build?.changes(project, previousBuild) }

            runBlocking {
                val details = testCaseExecution.details()

                invokeLater {
                    details.coveredLines.forEach { (fileName, lines) ->
                        model.add(CoveredFile(fileName, lines, changes?.changeForPartialFileName(fileName)))
                    }
                    coveredFilesList.model = model
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

    internal data class CoveredFile(val fileName: String, val lines: List<Int>, val vcsChange: Change?) {
        fun getFile(project: Project) = project.service<PsiService>().findFileByRelativePath(fileName)

        fun name() = fileName.substringAfterLast('/').substringBeforeLast('.')
    }

    override fun handleNewBuilds(buildHistory: List<Build>) {
        this.buildHistory = buildHistory
    }

    private fun TextualDiff.highlightChanges() = onEach { line ->
        line.innerFragments?.forEach {
            editor.highlightRange(
                line.startOffset2 + it.startOffset1,
                line.startOffset2 + it.endOffset2
            )
        }
    }

    private fun TextualDiff.giveOptionalDeletionsWarning() = deletions().let {
        if (it > 0) {
            diffInformationLabel.text = "$it code fragments were removed, see the full diff."
        }
    }
}

fun JButton.replaceActionListener(actionListener: ActionListener) {
    actionListeners.forEach { removeActionListener(it) }
    addActionListener(actionListener)
}
