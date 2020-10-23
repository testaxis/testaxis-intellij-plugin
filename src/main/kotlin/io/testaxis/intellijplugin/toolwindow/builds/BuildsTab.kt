package io.testaxis.intellijplugin.toolwindow.builds

import com.intellij.openapi.Disposable
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.panel
import io.testaxis.intellijplugin.toolwindow.builds.tree.BuildsTree
import io.testaxis.intellijplugin.toolwindow.builds.views.BuildDetailsRightView
import io.testaxis.intellijplugin.toolwindow.builds.views.RightView
import io.testaxis.intellijplugin.toolwindow.builds.views.TestCaseDetailsRightView
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
import javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED

private class RightViewStateManager(vararg val views: RightView) {
    init {
        hideAll()
    }

    fun hideAll() = views.forEach { it.hide() }

    inline fun <reified T : RightView> showAndGet(): T = views.filterIsInstance<T>().first().also {
        hideAll()
        it.show()
    }

    fun getPanels() = views.map { it.getPanel() }
}

class BuildsTab : Disposable {
    private val stateManager = RightViewStateManager(
        BuildDetailsRightView(),
        TestCaseDetailsRightView()
    )

    fun create(): JComponent {
        val splitter = OnePixelSplitter(.33f)

        splitter.firstComponent = JPanel(BorderLayout()).apply {
            add(JBScrollPane(createBuildsTreePanel(), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED))
        }

        splitter.secondComponent = JPanel(BorderLayout()).apply {
            // TODO: Properly add the right views here as scroll panes, no need for panel/row/cell
            add(panel {
                row {
                    cell(isVerticalFlow = true) {
                        stateManager.getPanels().forEach { it() }
                    }
                }
            })
        }

        return splitter
    }

    private fun createBuildsTreePanel() =
        BuildsTree().apply {
            buildSelectedListeners.add {
                stateManager.showAndGet<BuildDetailsRightView>().setBuild(it)
            }
            testCaseSelectedListeners.add {
                stateManager.showAndGet<TestCaseDetailsRightView>().setBuild(it)
            }
        }.let {
            ToolbarDecorator.createDecorator(it.createTree()).createPanel()
        }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}
