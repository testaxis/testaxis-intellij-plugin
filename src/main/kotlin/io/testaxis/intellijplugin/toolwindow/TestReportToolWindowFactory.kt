package io.testaxis.intellijplugin.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import io.testaxis.intellijplugin.toolwindow.builds.BuildsTab
import io.testaxis.intellijplugin.toolwindow.projectstatistics.ProjectStatisticsTab

class TestReportToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()

        toolWindow.contentManager.addContent(
            contentFactory.createContent(BuildsTab(project).create(), "Builds", false)
        )

        toolWindow.contentManager.addContent(
            contentFactory.createContent(ProjectStatisticsTab(toolWindow).content(), "Project Statistics", false)
        )
    }
}
