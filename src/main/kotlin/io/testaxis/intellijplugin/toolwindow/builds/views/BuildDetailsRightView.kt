package io.testaxis.intellijplugin.toolwindow.builds.views

import com.intellij.ui.components.Label
import com.intellij.ui.layout.panel
import io.testaxis.intellijplugin.toolwindow.builds.tree.FakeBuild

class BuildDetailsRightView : RightView {
    private val buildLabel = Label("the name of the build")

    private val panel = panel {
        row {
            buildLabel()
            label("test1")
            label("test1")
        }
    }

    override fun getPanel() = panel

    override fun hide() {
        panel.isVisible = false
    }

    override fun show() {
        panel.isVisible = true
    }

    fun setBuild(build: FakeBuild) = with(build) {
        buildLabel.text = name
    }
}
