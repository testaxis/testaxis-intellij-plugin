package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.ui.CollectionListModel
import com.intellij.ui.JBColor
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.panel
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.services.GitService
import io.testaxis.intellijplugin.services.PsiService
import io.testaxis.intellijplugin.toolwindow.builds.NotMatchingRevisionsWarning
import io.testaxis.intellijplugin.toolwindow.builds.views.BuildsUpdateHandler
import io.testaxis.intellijplugin.vcs.TextualDiff
import io.testaxis.intellijplugin.vcs.changes
import io.testaxis.intellijplugin.vcs.deletions
import io.testaxis.intellijplugin.vcs.findPreviousBuild
import io.testaxis.intellijplugin.vcs.textualDiff
import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer
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

                // TODO: Do something with new/moved files, or maybe just labels in the covered files list?
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
            // TODO: Add warning if previous build could not be found, most likely the build commit is not present locally
            val changes = previousBuild?.let { testCaseExecution.build?.changes(project, previousBuild) }

            println("Changes in build: $changes")

            runBlocking {
                val details = testCaseExecution.details()

                ApplicationManager.getApplication().invokeLater {
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

    private data class CoveredFile(val fileName: String, val lines: List<Int>, val vcsChange: Change?) {
        fun getFile(project: Project) = project.service<PsiService>().findFileByRelativePath(fileName)
    }

    private class CoveredFileCellRenderer : BorderLayoutPanel(), ListCellRenderer<CoveredFile> {

//        override fun getListCellRendererComponent(
//            list: JList<*>?,
//            value: Any?,
//            index: Int,
//            isSelected: Boolean,
//            cellHasFocus: Boolean
//        ): Component {
//            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus).apply {
//                value?.let {
//                    val coveredFile = it as CoveredFile
//
//                    text =  """
//                        <html>
//                            <strong>${coveredFile.fileName.substringAfterLast('/').substringBeforeLast('.')}</strong>
//                            <font color=green>${coveredFile.vcsChange?.type ?: ""}</font>
//                            <br />
//                            ${coveredFile.fileName}
//                        </html>
//                    """.trimIndent()
//                }
//            }
//        }

//        override fun customizeCellRenderer(
//            list: JList<out CoveredFile>,
//            value: CoveredFile?,
//            index: Int,
//            selected: Boolean,
//            hasFocus: Boolean
//        ) {
//            val coveredFile = value ?: return
////            append("""<html>
////                <strong>${coveredFile.fileName.substringAfterLast('/').substringBeforeLast('.')}</strong>
////                           <font color=green>${coveredFile.vcsChange?.type ?: ""}</font>                            <br />
////                            ${coveredFile.fileName}
////                        </html>
////                    """.trimIndent())
//            append(coveredFile.fileName.substringAfterLast('/').substringBeforeLast('.'), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
//            append(coveredFile.vcsChange?.type?.toString() ?: "", SimpleTextAttributes.REGULAR_ATTRIBUTES, 0, SwingConstants.RIGHT)
//        }

        protected var mySelected = false
        protected var myForeground: Color? = null
//        protected var mySelectionForeground: Color? = null

        override fun getListCellRendererComponent(
            list: JList<out CoveredFile>,
            coveredFile: CoveredFile,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean
        ): Component {
            mySelected = selected
            myForeground = if (!isEnabled) UIUtil.getLabelDisabledForeground() else {
                if (selected) list.selectionForeground else list.foreground
            }
//            mySelectionForeground = list.selectionForeground

            font = list.font
            background = if (UIUtil.isUnderWin10LookAndFeel()) {
                if (selected) list.selectionBackground else list.background
            } else {
                if (selected) list.selectionBackground else null
            }
            foreground = if (!isEnabled) UIUtil.getLabelDisabledForeground() else {
                if (selected) list.selectionForeground else list.foreground
            }

            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

            removeAll()
            add(BorderLayoutPanel().also {
                it.background = this.background
                it.foreground = this.foreground

                val shortFileName = coveredFile.fileName.substringAfterLast('/').substringBeforeLast('.')
                it.add(JLabel(shortFileName).also {
                    it.foreground = this.foreground
                    it.font = it.font.deriveFont(Font.BOLD)
                })

                coveredFile.vcsChange?.type?.let { changeType ->
                    it.addToRight(BorderLayoutPanel().also { panel ->
                        panel.background = changeType.color()
                        panel.border = BorderFactory.createEmptyBorder(2, 4, 2, 4)

                        panel.add(JLabel(changeType.label()).also {
                            it.foreground = Color.white
                        })
                    })
                }
            })

            addToBottom(JLabel(coveredFile.fileName).also {
                it.foreground = this.foreground.darker()
            })

            return this
        }

        private fun Change.Type.color() = when (this) {
            Change.Type.MODIFICATION -> JBColor.BLUE
            Change.Type.NEW -> JBColor.GREEN
            Change.Type.DELETED -> JBColor.RED
            Change.Type.MOVED -> JBColor.GRAY
        }

        private fun Change.Type.label() = when (this) {
            Change.Type.MODIFICATION -> "Modified"
            Change.Type.NEW -> "New"
            Change.Type.DELETED -> "Deleted"
            Change.Type.MOVED -> "Moved"
        }
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
