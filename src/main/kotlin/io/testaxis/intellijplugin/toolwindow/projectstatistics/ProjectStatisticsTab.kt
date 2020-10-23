package io.testaxis.intellijplugin.toolwindow.projectstatistics

import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.panel

class ProjectStatisticsTab(toolWindow: ToolWindow) {
    private var description = "This is a description"

//    fun content() = panel {
//        row("First row") {
//            noteRow("Login to get notified when the submitted exceptions are fixed.")
//            right {
//                link("Click here!") {
//                    BrowserUtil.browse("https://account.jetbrains.com/forgot-password")
//                }
//            }
//            noteRow(description)
//            label("aaewf")
//            stateflow
//        }
//        row("Second row") {
//            textField(::description)
//        }
//    }

    val logsModel = CollectionListModel<String>().apply {
        add("First element")
        add("Second element")
        add("Third element")
    }

    val logsPanel = panel {
        val jbList = JBList(logsModel)
        val decorator = ToolbarDecorator.createDecorator(jbList)
        val innerPanel = decorator.createPanel()
        row {
            innerPanel(CCFlags.grow, growPolicy = GrowPolicy.MEDIUM_TEXT)
        }
    }

    fun content() = logsPanel
}
