package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.CollectionListModel
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBList
import com.intellij.util.ui.components.BorderLayoutPanel
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.services.PsiService
import io.testaxis.intellijplugin.toolwindow.builds.NotMatchingRevisionsWarning
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListSelectionModel

private const val SPLITTER_PROPORTION_ONE_THIRD = .33f

class CodeUnderTestTab(val project: Project) : TestCaseTab {
    override val tabName = "Code Under Test"

    private val coveredFilesList = JBList(CollectionListModel<CoveredFile>()).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        cellRenderer = CoveredFileCellRenderer()
    }

    private val panel = BorderLayoutPanel()
    private val editor = TestCodeEditorField(project)

    init {
        coveredFilesList.addListSelectionListener {
            if (coveredFilesList.selectedValue != null) {
                editor.showFile(coveredFilesList.selectedValue.getFile(project))
                coveredFilesList.selectedValue.lines.forEach { editor.highlightLine(it) }
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
                }
            }
        )
    }

    override fun setTestCaseExecution(testCaseExecution: TestCaseExecution) {
        editor.showText("")

        val model = CollectionListModel<CoveredFile>()
        GlobalScope.launch {
            with(testCaseExecution.details()) {
                ApplicationManager.getApplication().invokeLater {
                    coveredLines.forEach { (fileName, lines) ->
                        model.add(CoveredFile(fileName, lines))
                        coveredFilesList.model = model
                    }
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

    private data class CoveredFile(val fileName: String, val lines: List<Int>) {
        fun getFile(project: Project) = project.service<PsiService>().findFileByRelativePath(fileName)
    }

    private class CoveredFileCellRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus).apply {
                value?.let {
                    text = (it as CoveredFile).fileName
                }
            }
        }
    }
}