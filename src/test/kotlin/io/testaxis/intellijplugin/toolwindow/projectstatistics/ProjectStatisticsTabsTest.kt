package io.testaxis.intellijplugin.toolwindow.projectstatistics

import io.testaxis.intellijplugin.IntelliJPlatformUITest
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.junit.jupiter.api.Test
import java.awt.Container

class ProjectStatisticsTabsTest : IntelliJPlatformUITest() {
    @Test
    fun `it shows the projects statistics label`() {
        val component = GuiActionRunner.execute<Container> { ProjectStatisticsTab().content() }
        frame = showInFrame(component)

        frame.requireContainsLabel("Coming soon")
    }
}
