package io.testaxis.intellijplugin.toolwindow.builds.views

import com.intellij.ui.layout.panel

class WelcomeRightView : RightView {
    private val panel = panel {
        row {
            label("Welcome to TestAxis!")
        }
    }

    override fun getPanel() = panel

    override fun hide() {
        panel.isVisible = false
    }

    override fun show() {
        panel.isVisible = true
    }
}
