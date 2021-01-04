package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.toolwindow.builds.NotMatchingRevisionsWarning
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class TestCodeTab(val project: Project) : TestCaseTab, Disposable {
    override val tabName = "Test Code"

    private lateinit var testCaseExecution: TestCaseExecution

    private val panel = JPanel(BorderLayout())
    private val testCaseCodeEditor = TestCodeEditorField(project)

    override fun activate() {
        testCaseExecution.getMethod(project).let { method ->
            testCaseCodeEditor.showTestMethod(method)
            testCaseCodeEditor.highlightElement(method)
        }
    }

    override fun getComponent(): JComponent = panel.apply {
        add(testCaseCodeEditor)
    }

    override fun setTestCaseExecution(testCaseExecution: TestCaseExecution) {
        this.testCaseExecution = testCaseExecution

        panel.components.filterIsInstance<NotMatchingRevisionsWarning>().forEach { panel.remove(it) }
        NotMatchingRevisionsWarning(project, testCaseExecution.build).let {
            if (it.shouldBeApplied()) {
                panel.add(it, BorderLayout.NORTH)
            }
        }
    }

    override fun dispose() {
        testCaseCodeEditor.editor?.let { EditorFactory.getInstance().releaseEditor(it) }
    }
}
