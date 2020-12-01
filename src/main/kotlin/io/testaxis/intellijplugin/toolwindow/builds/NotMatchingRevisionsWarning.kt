package io.testaxis.intellijplugin.toolwindow.builds

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.LightColors.YELLOW
import com.intellij.util.ui.UIUtil
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.services.GitService

class NotMatchingRevisionsWarning(val project: Project, val build: Build?) : EditorNotificationPanel(YELLOW) {

    fun shouldBeApplied(): Boolean {
        if (build == null) {
            return true
        }

        return build.commit != project.service<GitService>().currentCommit()
    }

    init {
        text(
            "The current checked out revision does not match the revision of this build. " +
                "The highlighted code below may be inaccurate."
        )
        icon(UIUtil.getBalloonWarningIcon())
        setProject(project)

        if (build != null) {
            createActionLabel("Checkout Branch ${build.branch}") {
                project.service<GitService>().checkout(build.branch)
            }
            createActionLabel("Checkout Revision ${build.shortCommitHash()}") {
                project.service<GitService>().checkout(build.commit)
            }
        }
    }
}
