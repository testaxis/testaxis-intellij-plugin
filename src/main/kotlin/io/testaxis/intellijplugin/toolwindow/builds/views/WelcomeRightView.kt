package io.testaxis.intellijplugin.toolwindow.builds.views

import com.intellij.ui.components.Label
import io.testaxis.intellijplugin.toolwindow.borderLayoutPanel
import io.testaxis.intellijplugin.toolwindow.vertical
import javax.swing.border.EmptyBorder

class WelcomeRightView : RightView {
    private val panel = borderLayoutPanel {
        addToTop(
            vertical(
                Label("Welcome to TestAxis!", bold = true),
                Label("Select a build on the left to view test results.")
            )
        )
    }.apply {
        border = EmptyBorder(20, 20, 20, 20)
    }

    override fun getPanel() = panel

    override fun hide() {
        panel.isVisible = false
    }

    override fun show() {
        panel.isVisible = true
    }
}
