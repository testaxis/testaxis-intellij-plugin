package io.testaxis.intellijplugin.toolwindow.projectstatistics

import io.testaxis.intellijplugin.IntelliJPlatformUITest
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.junit.jupiter.api.Test

class ProjectStatisticsTabsTest : IntelliJPlatformUITest() {
    @Test
    fun `it shows the projects statistics label`() {
        val component = GuiActionRunner.execute<ProjectStatisticsTab> { ProjectStatisticsTab() }
        frame = showInFrame(component.content())

        frame.requireContainsLabel("Project Statistics")
    }
}
