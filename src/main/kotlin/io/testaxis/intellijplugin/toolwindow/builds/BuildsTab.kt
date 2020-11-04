package io.testaxis.intellijplugin.toolwindow.builds

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.layout.panel
import com.intellij.util.ui.components.BorderLayoutPanel
import io.testaxis.intellijplugin.messages.BuildNotifier
import io.testaxis.intellijplugin.messages.MessageBusService
import io.testaxis.intellijplugin.services.TestAxisApiService
import io.testaxis.intellijplugin.services.TestAxisWebSocketService
import io.testaxis.intellijplugin.toolwindow.builds.tree.BuildsTree
import io.testaxis.intellijplugin.toolwindow.builds.views.BuildDetailsRightView
import io.testaxis.intellijplugin.toolwindow.builds.views.RightView
import io.testaxis.intellijplugin.toolwindow.builds.views.TestCaseDetailsRightView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.swing.JComponent

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

const val SPLITTER_PROPORTION_ONE_THIRD = .33f

class BuildsTab(project: Project) : Disposable {
    private val stateManager = RightViewStateManager(
        BuildDetailsRightView(),
        TestCaseDetailsRightView(project)
    )

    private val buildsTree = BuildsTree().apply {
        buildSelectedListeners.add {
            stateManager.showAndGet<BuildDetailsRightView>().setBuild(it)
        }
        testCaseSelectedListeners.add {
            stateManager.showAndGet<TestCaseDetailsRightView>().setTestCaseExecution(it)
        }
    }

    init {
        project.service<TestAxisWebSocketService>().subscribeToBuilds {
            updateBuilds()
        }

        project.service<MessageBusService>().run {
            bus.connect().subscribe(buildShouldBeSelectedTopic, BuildNotifier { buildsTree.selectAndExpand(it) })
        }
    }

    fun create(): JComponent {
        val splitter = OnePixelSplitter(SPLITTER_PROPORTION_ONE_THIRD)

        splitter.firstComponent = BorderLayoutPanel().apply {
            add(createBuildsTreePanel())
        }

        splitter.secondComponent = BorderLayoutPanel().apply {
            @Suppress("ForbiddenComment")
            // TODO: Properly add the right views here as scroll panes, no need for panel/row/cell
            add(
                panel {
                    row {
                        cell(isVerticalFlow = true) {
                            stateManager.getPanels().forEach { it() }
                        }
                    }
                }
            )
        }

        updateBuilds()

        return splitter
    }

    private fun createBuildsTreePanel() =
        ToolbarDecorator.createDecorator(buildsTree.render()).createPanel()

    private fun updateBuilds() = GlobalScope.launch {
        buildsTree.updateData(service<TestAxisApiService>().getBuilds())
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}
