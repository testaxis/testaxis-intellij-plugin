package io.testaxis.intellijplugin.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

const val TOOL_WINDOW_ID = "TestAxis"

class InspectBuildAction(private val project: Project, name: String) : NotificationAction(name) {
    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        ToolWindowManager.getInstance(project)
            .getToolWindow(TOOL_WINDOW_ID)
            ?.show()

        notification.expire()
    }
}
