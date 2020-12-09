package io.testaxis.intellijplugin.toolwindow

import com.intellij.ui.IconManager
import javax.swing.Icon

class Icons private constructor() {
    companion object {
        val Clock: Icon = IconManager.getInstance().getIcon("/icons/clock.svg", this::class.java)
        val Time: Icon = IconManager.getInstance().getIcon("/icons/time.svg", this::class.java)
    }
}
