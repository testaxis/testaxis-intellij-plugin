package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.Label
import com.intellij.ui.layout.panel
import io.testaxis.intellijplugin.models.TestCaseExecution
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.swing.JComponent
import javax.swing.JTextArea

class DetailsTab(val project: Project) : TestCaseTab {
    override val tabName = "Execution Details"

    private lateinit var testCaseExecution: TestCaseExecution

    private val nameLabel = Label("")
    private val testSuiteNameLabel = Label("")
    private val classNameLabel = Label("")
    private val timeLabel = Label("")
    private val createdAtLabel = Label("")
    private val failureMessageTextArea = JTextArea(3, 10)
    private val failureTypeLabel = Label("")
    private val failureContentTextArea = JTextArea(8, 10)

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
    }

    override fun getComponent(): JComponent = panel

    override fun setTestCaseExecution(testCaseExecution: TestCaseExecution) {
        this.testCaseExecution = testCaseExecution

        with(testCaseExecution) {
            nameLabel.text = name
            testSuiteNameLabel.text = testSuiteName
            classNameLabel.text = className
            timeLabel.text = time.toString()
            createdAtLabel.text = createdAt.toString()
        }
    }

    override fun activate() {
        GlobalScope.launch {
            with(testCaseExecution.details()) {
                ApplicationManager.getApplication().invokeLater {
                    failureMessageTextArea.text = failureMessage
                    failureTypeLabel.text = failureType
                    failureContentTextArea.text = failureContent
                }
            }
        }
    }
}
