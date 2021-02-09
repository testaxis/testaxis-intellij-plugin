package io.testaxis.intellijplugin.toolwindow.builds

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.Label
import com.intellij.util.ui.components.BorderLayoutPanel
import io.testaxis.intellijplugin.messages.MessageConfiguration
import io.testaxis.intellijplugin.services.ApiService
import io.testaxis.intellijplugin.services.GitService
import io.testaxis.intellijplugin.services.SettingsNotInitializedException
import io.testaxis.intellijplugin.toolwindow.Icons
import io.testaxis.intellijplugin.toolwindow.builds.filters.BranchFilter
import io.testaxis.intellijplugin.toolwindow.builds.filters.PrFilter
import io.testaxis.intellijplugin.toolwindow.builds.filters.StatusFilter
import io.testaxis.intellijplugin.toolwindow.builds.tree.BuildsTree
import io.testaxis.intellijplugin.toolwindow.builds.views.BuildDetailsRightView
import io.testaxis.intellijplugin.toolwindow.builds.views.BuildsUpdateHandler
import io.testaxis.intellijplugin.toolwindow.builds.views.RightView
import io.testaxis.intellijplugin.toolwindow.builds.views.TestCaseDetailsRightView
import io.testaxis.intellijplugin.toolwindow.builds.views.WelcomeRightView
import io.testaxis.intellijplugin.toolwindow.horizontal
import kotlinx.coroutines.runBlocking
import java.awt.CardLayout
import javax.swing.JComponent

private const val SPLITTER_PROPORTION_ONE_THIRD = .33f

class BuildsTab(val project: Project) : Disposable {
    private val stateManager = RightViewStateManager(
        WelcomeRightView(project),
        BuildDetailsRightView(project),
        TestCaseDetailsRightView(project)
    )

    private val branchFilter = BranchFilter(project) { updateBuilds() }
    private val filters = listOf(
        branchFilter,
        StatusFilter { updateBuilds() },
        PrFilter { updateBuilds() },
    )

    private val buildsTree = BuildsTree(project).apply {
        buildSelectedListeners.add {
            stateManager.showAndGet<BuildDetailsRightView>().setBuild(it)
        }
        testCaseSelectedListeners.add {
            stateManager.showAndGet<TestCaseDetailsRightView>().setTestCaseExecution(it)
        }
    }

    init {
        project.messageBus.connect().subscribe(
            MessageConfiguration.BUILD_FINISHED_TOPIC,
            MessageConfiguration.BuildNotifier { updateBuilds() }
        )

        project.messageBus.connect().subscribe(
            MessageConfiguration.BUILD_SHOULD_BE_SELECTED_TOPIC,
            MessageConfiguration.BuildNotifier { buildsTree.selectAndExpand(it) }
        )

        project.messageBus.connect().subscribe(
            MessageConfiguration.API_SETTINGS_UPDATED_TOPIC,
            MessageConfiguration.ApiSettingsNotifier { updateBuilds() }
        )

        project.service<GitService>().pluginCheckoutListeners.add {
            buildsTree.selectedTestCase()?.let {
                stateManager.showAndGet<TestCaseDetailsRightView>().setTestCaseExecution(it)
            }
        }
    }

    fun create(): JComponent {
        val splitter = OnePixelSplitter(SPLITTER_PROPORTION_ONE_THIRD)

        splitter.firstComponent = BorderLayoutPanel().apply {
            addToTop(horizontal(Label("").apply { icon = Icons.TestAxisMedium }, createToolbar(), hgap = 0))
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

    private fun createToolbar(): JComponent {
        val mainGroup = DefaultActionGroup().apply {
            filters.forEach { add(it.actionComponent) }
        }
        return ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, mainGroup, true).component
    }

    private fun createBuildsTreePanel() =
        ToolbarDecorator.createDecorator(buildsTree.render()).createPanel()

    private fun updateBuilds(): Unit = runBackgroundableTask("Retrieving builds", project, cancellable = false) {
        runBlocking {
            try {
                val builds = service<ApiService>().withProject(project).getBuilds()

                builds.filter { it.commitMessage == null }.forEach {
                    project.service<GitService>().retrieveCommitMessage(it.commit, ignoreErrors = true)
                        ?.let { message -> it.commitMessage = message }
                }

                runInEdt {
                    buildsTree.updateData(builds.filter { build -> filters.all { it.filter(build) } })
                }

                branchFilter.updateBranches(builds.map { it.branch }.distinct())

                stateManager.views.filterIsInstance<BuildsUpdateHandler>().forEach { it.handleNewBuilds(builds) }
            } catch (exception: SettingsNotInitializedException) {
                println("Settings have not been initialized yet.")
            }
        }
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}

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
