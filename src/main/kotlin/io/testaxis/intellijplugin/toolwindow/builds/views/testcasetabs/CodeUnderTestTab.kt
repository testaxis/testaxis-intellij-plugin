package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.ui.layout.panel
import io.testaxis.intellijplugin.models.TestCaseExecution
import javax.swing.JComponent

class CodeUnderTestTab : TestCaseTab {
    override val tabName = "Code Under Test"

    override fun activate() {
        // Not yet implemented.
    }

    override fun getComponent(): JComponent = panel {
        row {
            label("Coverage will be shown here.")
        }
    }

    override fun setTestCaseExecution(testCaseExecution: TestCaseExecution) {
        // Not yet implemented.
    }
}
