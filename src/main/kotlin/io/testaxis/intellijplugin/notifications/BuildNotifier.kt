package io.testaxis.intellijplugin.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.actions.InspectBuildAction
import io.testaxis.intellijplugin.models.BuildStatus
import io.testaxis.intellijplugin.services.TestAxisWebSocketService

class BuildNotifier(val project: Project) {
    private val failedNotificationGroup = NotificationGroup(
        "TestAxis | Failed Builds",
        NotificationDisplayType.BALLOON,
    )
    private val successNotificationGroup = NotificationGroup(
        "TestAxis | Succeeded Builds",
        NotificationDisplayType.BALLOON,
        isLogByDefault = false
    )

    fun listenForNewBuilds() {
        project.service<TestAxisWebSocketService>().subscribeToBuilds {
            when (it.status) {
                BuildStatus.SUCCESS -> notify(it.label(), "The build passed.")
                BuildStatus.BUILD_FAILED -> notifyError(it.label(), "The build failed (not due to failing tests).")
                BuildStatus.TESTS_FAILED -> notifyWarning(it.label(), "The build failed due to failing tests.")
                BuildStatus.UNKNOWN -> notify(it.label(), "The status of this build is unknown.")
            }
        }
    }

    private fun notify(title: String, content: String) =
        successNotificationGroup.createNotification(title, content, NotificationType.INFORMATION)
            .addInspectBuildAction("Inspect build")
            .notify(project)

    private fun notifyError(title: String, content: String) =
        failedNotificationGroup.createNotification(title, content, NotificationType.ERROR)
            .addInspectBuildAction("Inspect test results")
            .notify(project)

    private fun notifyWarning(title: String, content: String) =
        failedNotificationGroup.createNotification(title, content, NotificationType.WARNING)
            .addInspectBuildAction("Inspect build")
            .notify(project)

    private fun Notification.addInspectBuildAction(name: String) = addAction(InspectBuildAction(project, name))
}
