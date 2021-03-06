package io.testaxis.intellijplugin.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import io.testaxis.intellijplugin.messages.MessageConfiguration
import io.testaxis.intellijplugin.models.Build

const val TOOL_WINDOW_ID = "TestAxis"

class InspectBuildAction(
    private val project: Project,
    val name: String,
    val build: Build
) : NotificationAction(name) {

    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        project.service<ToolWindowManager>().getToolWindow(TOOL_WINDOW_ID)?.show()

        project.messageBus.syncPublisher(MessageConfiguration.BUILD_SHOULD_BE_SELECTED_TOPIC).notify(build)

        notification.expire()
    }
}
