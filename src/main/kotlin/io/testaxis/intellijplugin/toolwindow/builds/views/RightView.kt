package io.testaxis.intellijplugin.toolwindow.builds.views

import javax.swing.JComponent

interface RightView {
    fun show()
    fun hide()
    fun getPanel(): JComponent
}
