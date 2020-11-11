package io.testaxis.intellijplugin.toolwindow.builds.views

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.ui.components.Label
import com.intellij.ui.layout.panel
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.toolwindow.builds.views.builds.TestCaseEditorField
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.swing.JTextArea

class TestCaseDetailsRightView(val project: Project) : RightView, Disposable {
    private lateinit var testCaseExecution: TestCaseExecution

    private val nameLabel = Label("")
    private val testSuiteNameLabel = Label("")
    private val classNameLabel = Label("")
    private val timeLabel = Label("")
    private val createdAtLabel = Label("")
    private val failureMessageTextArea = JTextArea(3, 10)
    private val failureTypeLabel = Label("")
    private val failureContentTextArea = JTextArea(8, 10)

    private val testCaseCodeEditor = TestCaseEditorField(project)

    private val panel = panel {
        row {
            label("Name:")
            nameLabel()
            button("Open Test") {
                testCaseExecution.getMethod(project)?.navigate(true)
            }
        }
        row {
            label("Test Suite Name:")
            testSuiteNameLabel()
        }
        row {
            label("Class Name:")
            classNameLabel()
        }
        row {
            label("Time:")
            timeLabel()
        }
        row {
            label("Run at:")
            createdAtLabel()
        }
        row {
            label("Failure message:")
            failureMessageTextArea()
        }
        row {
            label("Failure type:")
            failureTypeLabel()
        }
        row {
            label("Failure content:")
            failureContentTextArea()
        }
        row {
            label("Test code:")
            testCaseCodeEditor()
        }
    }

    override fun getPanel() = panel

    override fun hide() {
        panel.isVisible = false
    }

    override fun show() {
        panel.isVisible = true
    }

    fun setTestCaseExecution(testCaseExecution: TestCaseExecution) {
        this.testCaseExecution = testCaseExecution

        with(testCaseExecution) {
            nameLabel.text = name
            testSuiteNameLabel.text = testSuiteName
            classNameLabel.text = className
            timeLabel.text = time.toString()
            createdAtLabel.text = createdAt.toString()
        }

        GlobalScope.launch {
            with(testCaseExecution.details()) {
                ApplicationManager.getApplication().invokeLater {
                    failureMessageTextArea.text = failureMessage
                    failureTypeLabel.text = failureType
                    failureContentTextArea.text = failureContent
                }
            }
        }

        testCaseCodeEditor.showTestMethod(testCaseExecution.getMethod(project))
    }

    override fun dispose() {
        testCaseCodeEditor.editor?.let { EditorFactory.getInstance().releaseEditor(it) }
    }
}
