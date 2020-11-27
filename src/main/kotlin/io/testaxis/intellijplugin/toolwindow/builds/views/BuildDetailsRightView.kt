package io.testaxis.intellijplugin.toolwindow.builds.views

import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.layout.panel
import io.testaxis.intellijplugin.models.Build

class BuildDetailsRightView : RightView {
    private val buildLabel = SimpleColoredComponent()

    private val panel = panel {
        row("Build") {
            buildLabel()
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
    }
}
