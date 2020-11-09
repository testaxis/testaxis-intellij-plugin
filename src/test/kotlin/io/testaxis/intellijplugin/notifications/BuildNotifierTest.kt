package io.testaxis.intellijplugin.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import io.testaxis.intellijplugin.FakeWebSocketService
import io.testaxis.intellijplugin.Fakes
import io.testaxis.intellijplugin.IntelliJPlatformTest
import io.testaxis.intellijplugin.actions.InspectBuildAction
import io.testaxis.intellijplugin.fakeBuild
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.BuildStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class BuildNotifierTest : IntelliJPlatformTest() {
    private val webSocketService = FakeWebSocketService()

    private var notification: Notification? = null

    override fun getFakes() = Fakes(webSocketService = webSocketService)

    @BeforeEach
    override fun setUp() {
        super.setUp()

        notification = null

        fixture.project.messageBus.connect().subscribe(
            Notifications.TOPIC,
            object : Notifications {
                override fun notify(incomingNotification: Notification) {
                    notification = incomingNotification
                }
            }
        )
    }

    private fun getIncomingNotificationOrFail(): Notification {
        expectThat(notification).isNotNull()

        return notification!!
    }

    @Test
    fun `it notifies the developer of a passing build`() {
        val newBuild = fakeBuild(status = BuildStatus.SUCCESS)
        webSocketService.reportNewBuild(newBuild)

        with(getIncomingNotificationOrFail()) {
            expectThat(groupId) isEqualTo BuildNotifier.SUCCESS_NOTIFICATION_GROUP_ID
            expectThat(type) isEqualTo NotificationType.INFORMATION
            expectThat(icon) isEqualTo BuildStatus.SUCCESS.icon
            expectThat(title) isEqualTo newBuild.label()

            expectThat(actions) consistsOfInspectBuildAction newBuild
        }
    }

    @Test
    fun `it notifies the developer of a failed build`() {
        val newBuild = fakeBuild(status = BuildStatus.BUILD_FAILED)
        webSocketService.reportNewBuild(newBuild)

        with(getIncomingNotificationOrFail()) {
            expectThat(groupId) isEqualTo BuildNotifier.FAILED_NOTIFICATION_GROUP_ID
            expectThat(type) isEqualTo NotificationType.WARNING
            expectThat(icon) isEqualTo BuildStatus.BUILD_FAILED.icon
            expectThat(title) isEqualTo newBuild.label()

            expectThat(actions) consistsOfInspectBuildAction newBuild
        }
    }

    @Test
    fun `it notifies the developer of a build with failed tests`() {
        val newBuild = fakeBuild(status = BuildStatus.TESTS_FAILED)
        webSocketService.reportNewBuild(newBuild)

        with(getIncomingNotificationOrFail()) {
            expectThat(groupId) isEqualTo BuildNotifier.FAILED_NOTIFICATION_GROUP_ID
            expectThat(type) isEqualTo NotificationType.ERROR
            expectThat(icon) isEqualTo BuildStatus.TESTS_FAILED.icon
            expectThat(title) isEqualTo newBuild.label()

            expectThat(actions) consistsOfInspectBuildAction newBuild
        }
    }

    @Test
    fun `it notifies the developer of a build with an unknown status`() {
        val newBuild = fakeBuild(status = BuildStatus.UNKNOWN)
        webSocketService.reportNewBuild(newBuild)

        with(getIncomingNotificationOrFail()) {
            expectThat(groupId) isEqualTo BuildNotifier.SUCCESS_NOTIFICATION_GROUP_ID
            expectThat(type) isEqualTo NotificationType.INFORMATION
            expectThat(icon) isEqualTo BuildStatus.UNKNOWN.icon
            expectThat(title) isEqualTo newBuild.label()

            expectThat(actions) consistsOfInspectBuildAction newBuild
        }
    }

    private infix fun Assertion.Builder<List<AnAction>>.consistsOfInspectBuildAction(build: Build) =
        assert("consists of InspectBuildAction with the correct build") {
            expectThat(it).hasSize(1)
                .get { first() }.isA<InspectBuildAction>()
                .get { build }.isEqualTo(build)
        }
}
