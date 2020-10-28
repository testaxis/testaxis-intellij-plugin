package io.testaxis.intellijplugin.toolwindow.builds.views

import com.intellij.ui.components.Label
import com.intellij.ui.layout.panel
import io.testaxis.intellijplugin.TestCaseExecution
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.swing.JTextArea

class TestCaseDetailsRightView : RightView {
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

    override fun getPanel() = panel

    override fun hide() {
        panel.isVisible = false
    }

    override fun show() {
        panel.isVisible = true
    }

    fun setTestCaseExecution(testCaseExecution: TestCaseExecution) {
        nameLabel.text = "Loading..."
        GlobalScope.launch {
            with(testCaseExecution.details()) {
                nameLabel.text = name
                testSuiteNameLabel.text = testSuiteName
                classNameLabel.text = className
                timeLabel.text = time.toString()
                createdAtLabel.text = createdAt.toString()
                failureMessageTextArea.text = failureMessage
                failureTypeLabel.text = failureType
                failureContentTextArea.text = failureContent
            }
        }
    }
}
