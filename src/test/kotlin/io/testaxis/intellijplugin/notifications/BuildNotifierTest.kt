package io.testaxis.intellijplugin.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import io.testaxis.intellijplugin.IntelliJPlatformTest
import io.testaxis.intellijplugin.actions.InspectBuildAction
import io.testaxis.intellijplugin.fakeBuild
import io.testaxis.intellijplugin.messages.MessageConfiguration
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
    private var notification: Notification? = null

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

    private fun reportNewBuild(build: Build) {
        fixture.project.messageBus.syncPublisher(MessageConfiguration.BUILD_FINISHED_TOPIC).notify(build)
    }

    private fun getIncomingNotificationOrFail(): Notification {
        expectThat(notification).isNotNull()

        return notification!!
    }

    @Test
    fun `it notifies the developer of a passing build`() {
        val newBuild = fakeBuild(status = BuildStatus.SUCCESS)
        reportNewBuild(newBuild)

        with(getIncomingNotificationOrFail()) {
            expectThat(groupId) isEqualTo BuildNotifier.SUCCESS_NOTIFICATION_GROUP_ID
            expectThat(type) isEqualTo NotificationType.INFORMATION
            expectThat(icon) isEqualTo BuildStatus.SUCCESS.icon
            expectThat(title) isEqualTo newBuild.labelMaker().createString()

            expectThat(actions) consistsOfInspectBuildAction newBuild
        }
    }

    @Test
    fun `it notifies the developer of a failed build`() {
        val newBuild = fakeBuild(status = BuildStatus.BUILD_FAILED)
        reportNewBuild(newBuild)

        with(getIncomingNotificationOrFail()) {
            expectThat(groupId) isEqualTo BuildNotifier.FAILED_NOTIFICATION_GROUP_ID
            expectThat(type) isEqualTo NotificationType.WARNING
            expectThat(icon) isEqualTo BuildStatus.BUILD_FAILED.icon
            expectThat(title) isEqualTo newBuild.labelMaker().createString()

            expectThat(actions) consistsOfInspectBuildAction newBuild
        }
    }

    @Test
    fun `it notifies the developer of a build with failed tests`() {
        val newBuild = fakeBuild(status = BuildStatus.TESTS_FAILED)
        reportNewBuild(newBuild)

        with(getIncomingNotificationOrFail()) {
            expectThat(groupId) isEqualTo BuildNotifier.FAILED_NOTIFICATION_GROUP_ID
            expectThat(type) isEqualTo NotificationType.ERROR
            expectThat(icon) isEqualTo BuildStatus.TESTS_FAILED.icon
            expectThat(title) isEqualTo newBuild.labelMaker().createString()

            expectThat(actions) consistsOfInspectBuildAction newBuild
        }
    }

    @Test
    fun `it notifies the developer of a build with an unknown status`() {
        val newBuild = fakeBuild(status = BuildStatus.UNKNOWN)
        reportNewBuild(newBuild)

        with(getIncomingNotificationOrFail()) {
            expectThat(groupId) isEqualTo BuildNotifier.SUCCESS_NOTIFICATION_GROUP_ID
            expectThat(type) isEqualTo NotificationType.INFORMATION
            expectThat(icon) isEqualTo BuildStatus.UNKNOWN.icon
            expectThat(title) isEqualTo newBuild.labelMaker().createString()

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
