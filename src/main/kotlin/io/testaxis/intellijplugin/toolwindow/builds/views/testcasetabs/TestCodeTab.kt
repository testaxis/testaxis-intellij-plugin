package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.models.TestCaseExecution
import javax.swing.JComponent

class TestCodeTab(val project: Project) : TestCaseTab, Disposable {
    override val tabName = "Test Code"

    private lateinit var testCaseExecution: TestCaseExecution

    private val testCaseCodeEditor = TestCodeEditorField(project)

    override fun activate() {
        testCaseExecution.getMethod(project)?.let { method ->
            testCaseCodeEditor.showTestMethod(method)
            testCaseCodeEditor.highlightElement(method)
        }
    }

    override fun getComponent(): JComponent = testCaseCodeEditor

    override fun setTestCaseExecution(testCaseExecution: TestCaseExecution) {
        this.testCaseExecution = testCaseExecution
    }

    override fun dispose() {
        testCaseCodeEditor.editor?.let { EditorFactory.getInstance().releaseEditor(it) }
    }
}
