package io.testaxis.intellijplugin.toolwindow.builds.views

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBTabsPaneImpl
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.TabsListener
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs.CodeUnderTestTab
import io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs.DetailsTab
import io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs.TestCaseTab
import io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs.TestCodeTab
import javax.swing.JComponent
import javax.swing.SwingConstants

class TestCaseDetailsRightView(val project: Project) : RightView, BuildsUpdateHandler, Disposable {
    private val tabbedPane = JBTabsPaneImpl(project, SwingConstants.TOP, this)

    private val tabs = listOf(DetailsTab(project), TestCodeTab(project), CodeUnderTestTab(project))

    init {
        tabs.forEach {
            tabbedPane.tabs.addTab(TabInfo(it.getComponent()).setText(it.tabName).setObject(it))
        }

        tabbedPane.tabs.addListener(
            object : TabsListener {
                override fun selectionChanged(oldSelection: TabInfo, newSelection: TabInfo) {
                    (newSelection.`object` as TestCaseTab).activate()
                }
            }
        )
    }

    override fun getPanel(): JComponent = tabbedPane.component

    override fun hide() {
        tabbedPane.component.isVisible = false
    }

    override fun show() {
        tabbedPane.selectedIndex = 0
        tabbedPane.component.isVisible = true
    }

    fun setTestCaseExecution(testCaseExecution: TestCaseExecution) {
        tabs.forEach {
            it.setTestCaseExecution(testCaseExecution)
        }

        tabs.firstOrNull()?.activate()
    }

    override fun dispose() = tabs.forEach {
        if (it is Disposable) {
            it.dispose()
        }
    }

    override fun handleNewBuilds(buildHistory: List<Build>) =
        tabs.filterIsInstance<BuildsUpdateHandler>().forEach { it.handleNewBuilds(buildHistory) }
}
