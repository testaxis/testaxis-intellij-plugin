package io.testaxis.intellijplugin.toolwindow.builds

import com.intellij.openapi.project.Project
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.LightColors.YELLOW
import com.intellij.util.ui.UIUtil

class NoPreviousBuildWarning(val project: Project) : EditorNotificationPanel(YELLOW) {
    init {
        text(
            "The previous build of this test could not be found. " +
                "Is the build commit of the current build present locally?"
        )
        icon(UIUtil.getBalloonWarningIcon())
        setProject(project)
    }
}
