package io.testaxis.intellijplugin.toolwindow.builds.views

import com.intellij.ui.components.Label
import com.intellij.ui.layout.panel
import io.testaxis.intellijplugin.TestCaseExecution

class TestCaseDetailsRightView : RightView {
    private val testLabel = Label("the name of the test")

    private val panel = panel {
        row {
            testLabel()
            label("test1")
            label("test1")
        }
    }

    override fun getPanel() = panel

    override fun hide() {
        panel.isVisible = false
    }

    override fun show() {
        panel.isVisible = true
    }

    fun setBuild(build: TestCaseExecution) = with(build) {
        testLabel.text = name
    }
}
