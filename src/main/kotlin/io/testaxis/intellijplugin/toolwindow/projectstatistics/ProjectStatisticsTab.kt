package io.testaxis.intellijplugin.toolwindow.projectstatistics

import com.intellij.ui.CollectionListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.panel

class ProjectStatisticsTab {
    private var description = "This is a description"

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
            label("Project Statistics")
            innerPanel(CCFlags.grow, growPolicy = GrowPolicy.MEDIUM_TEXT)
        }
    }

    fun content() = logsPanel
}
