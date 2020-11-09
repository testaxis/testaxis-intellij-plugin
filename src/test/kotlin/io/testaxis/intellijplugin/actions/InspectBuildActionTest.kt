package io.testaxis.intellijplugin.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import io.testaxis.intellijplugin.IntelliJPlatformTest
import io.testaxis.intellijplugin.fakeBuild
import io.testaxis.intellijplugin.messages.MessageConfiguration
import io.testaxis.intellijplugin.models.Build
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class InspectBuildActionTest : IntelliJPlatformTest() {
    @Test
    fun `it allows to set a custom name of the action`() {
        val action = InspectBuildAction(
            fixture.project,
            "The name of the action",
            fakeBuild()
        )

        expectThat(action.name) isEqualTo "The name of the action"
    }

    @Test
    fun `it notifies that the build should be selected`() {
        var buildThatShouldBeSelected: Build? = null
        fixture.project.messageBus.connect().subscribe(
            MessageConfiguration.BUILD_SHOULD_BE_SELECTED_TOPIC,
            MessageConfiguration.BuildNotifier {
                buildThatShouldBeSelected = it
            }
        )

        val notification = Notification("group", "title", "content", NotificationType.INFORMATION)
        val build = fakeBuild()
        val action = InspectBuildAction(
            fixture.project,
            "The name of the action",
            build
        )

        Notification.fire(notification, action)

        expectThat(buildThatShouldBeSelected).isNotNull().isEqualTo(build)
    }
}
