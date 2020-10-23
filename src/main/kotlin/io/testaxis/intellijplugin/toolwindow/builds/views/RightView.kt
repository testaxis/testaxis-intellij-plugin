package io.testaxis.intellijplugin.toolwindow.builds.views

import com.intellij.openapi.ui.DialogPanel

interface RightView {
    fun show()
    fun hide()
    fun getPanel(): DialogPanel
}
