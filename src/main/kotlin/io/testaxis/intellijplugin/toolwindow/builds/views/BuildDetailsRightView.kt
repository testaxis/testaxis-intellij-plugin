package io.testaxis.intellijplugin.toolwindow.builds.views

import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.layout.panel
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.vcs.findPreviousBuild

class BuildDetailsRightView(val project: Project) : RightView, BuildsUpdateHandler {
    private val buildLabel = SimpleColoredComponent()
    private val previousBuildLabel = SimpleColoredComponent()

    private var buildHistory: List<Build> = emptyList()

    private val panel = panel {
        row("Build") {
            buildLabel()
        }
        row("Previous build") {
            previousBuildLabel()
        }
    }

    override fun getPanel() = panel

    override fun hide() {
        panel.isVisible = false
    }

    override fun show() {
        panel.isVisible = true
    }

    fun setBuild(build: Build) {
        buildLabel.clear()
        build.labelMaker().createItems().forEach { buildLabel.append(it.text, it.attributes) }

        setPreviousBuildLabel(build)
    }

    private fun setPreviousBuildLabel(build: Build) {
        previousBuildLabel.clear()
        previousBuildLabel.append("loading...")

        runBackgroundableTask("Discovering previous build", project) {
            val previousBuild = build.findPreviousBuild(project, buildHistory)

            previousBuildLabel.clear()
            if (previousBuild != null) {
                previousBuild.labelMaker().createItems().forEach { previousBuildLabel.append(it.text, it.attributes) }
            } else {
                previousBuildLabel.append("Could not be found")
            }
        }
    }

    override fun handleNewBuilds(buildHistory: List<Build>) {
        this.buildHistory = buildHistory
    }
}
