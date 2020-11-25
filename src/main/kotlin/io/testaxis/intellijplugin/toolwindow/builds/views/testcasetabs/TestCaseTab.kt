package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import io.testaxis.intellijplugin.models.TestCaseExecution
import javax.swing.JComponent

interface TestCaseTab {
    val tabName: String

    fun activate()

    fun getComponent(): JComponent

    fun setTestCaseExecution(testCaseExecution: TestCaseExecution)
}
