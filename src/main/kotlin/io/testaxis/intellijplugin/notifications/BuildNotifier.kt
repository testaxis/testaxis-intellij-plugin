package io.testaxis.intellijplugin.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.actions.InspectBuildAction
import io.testaxis.intellijplugin.models.Build
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
                BuildStatus.SUCCESS -> notify(it, "The build passed.")
                BuildStatus.BUILD_FAILED -> notifyError(it, "The build failed (not due to failing tests).")
                BuildStatus.TESTS_FAILED -> notifyWarning(it, "The build failed due to failing tests.")
                BuildStatus.UNKNOWN -> notify(it, "The status of this build is unknown.")
            }
        }
    }

    private fun notify(build: Build, message: String) =
        successNotificationGroup.createNotification(build.label(), message, NotificationType.INFORMATION)
            .setIcon(build.status.icon)
            .addInspectBuildAction("Inspect build", build)
            .notify(project)

    private fun notifyError(build: Build, message: String) =
        failedNotificationGroup.createNotification(build.label(), message, NotificationType.ERROR)
            .addInspectBuildAction("Inspect test results", build)
            .notify(project)

    private fun notifyWarning(build: Build, message: String) =
        failedNotificationGroup.createNotification(build.label(), message, NotificationType.WARNING)
            .addInspectBuildAction("Inspect build", build)
            .notify(project)

    private fun Notification.addInspectBuildAction(name: String, build: Build) =
        addAction(InspectBuildAction(project, name, build))
}
