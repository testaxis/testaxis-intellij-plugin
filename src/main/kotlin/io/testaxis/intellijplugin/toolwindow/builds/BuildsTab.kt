package io.testaxis.intellijplugin.toolwindow.builds

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.util.ui.components.BorderLayoutPanel
import io.testaxis.intellijplugin.messages.MessageConfiguration
import io.testaxis.intellijplugin.services.ApiService
import io.testaxis.intellijplugin.services.GitService
import io.testaxis.intellijplugin.services.WebSocketService
import io.testaxis.intellijplugin.toolwindow.builds.tree.BuildsTree
import io.testaxis.intellijplugin.toolwindow.builds.views.BuildDetailsRightView
import io.testaxis.intellijplugin.toolwindow.builds.views.RightView
import io.testaxis.intellijplugin.toolwindow.builds.views.TestCaseDetailsRightView
import io.testaxis.intellijplugin.toolwindow.builds.views.WelcomeRightView
import kotlinx.coroutines.runBlocking
import java.awt.CardLayout
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

class BuildsTab(val project: Project) : Disposable {
    private val stateManager = RightViewStateManager(
        WelcomeRightView(),
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
        project.service<WebSocketService>().subscribeToBuilds {
            updateBuilds()
        }

        project.messageBus.connect().subscribe(
            MessageConfiguration.BUILD_SHOULD_BE_SELECTED_TOPIC,
            MessageConfiguration.BuildNotifier { buildsTree.selectAndExpand(it) }
        )
    }

    fun create(): JComponent {
        val splitter = OnePixelSplitter(SPLITTER_PROPORTION_ONE_THIRD)

        splitter.firstComponent = BorderLayoutPanel().apply {
            add(createBuildsTreePanel())
        }

        splitter.secondComponent = BorderLayoutPanel().apply {
            layout = CardLayout()
            stateManager.getPanels().forEach {
                add(it)
            }
        }

        updateBuilds()

        return splitter
    }

    private fun createBuildsTreePanel() =
        ToolbarDecorator.createDecorator(buildsTree.render()).createPanel()

    private fun updateBuilds() =
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Retrieving builds", false) {
                override fun run(indicator: ProgressIndicator) {
                    runBlocking {
                        val builds = service<ApiService>().getBuilds()

                        project.service<GitService>().retrieveCommitMessages(builds.map { it.commit }.distinct())
                            .let { messages -> builds.forEach { it.commitMessage = messages[it.commit] } }

                        buildsTree.updateData(builds)
                    }
                }
            }
        )

    override fun dispose() {
        TODO("Not yet implemented")
    }
}
