package io.testaxis.intellijplugin.toolwindow

import com.intellij.ui.IconManager
import javax.swing.Icon

class Icons private constructor() {
    companion object {
        val Clock: Icon = IconManager.getInstance().getIcon("/icons/clock.svg", this::class.java)
        val Time: Icon = IconManager.getInstance().getIcon("/icons/time.svg", this::class.java)

        val TestAxis: Icon = IconManager.getInstance().getIcon("/icons/testaxis.svg", this::class.java)
        val TestAxisMedium: Icon = IconManager.getInstance().getIcon("/icons/testaxis_medium.svg", this::class.java)
        val TestAxisSmall: Icon = IconManager.getInstance().getIcon("/icons/testaxis_small.svg", this::class.java)
    }
}
