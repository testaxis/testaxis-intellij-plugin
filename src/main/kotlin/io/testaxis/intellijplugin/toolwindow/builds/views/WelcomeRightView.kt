package io.testaxis.intellijplugin.toolwindow.builds.views

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.ui.components.Label
import io.testaxis.intellijplugin.messages.MessageConfiguration
import io.testaxis.intellijplugin.services.ApiService
import io.testaxis.intellijplugin.services.SettingsNotInitializedException
import io.testaxis.intellijplugin.services.UserNotAuthenticatedException
import io.testaxis.intellijplugin.settings.SettingsState
import io.testaxis.intellijplugin.toolwindow.Icons
import io.testaxis.intellijplugin.toolwindow.borderLayoutPanel
import io.testaxis.intellijplugin.toolwindow.vertical
import kotlinx.coroutines.runBlocking
import javax.swing.border.EmptyBorder

class WelcomeRightView(val project: Project) : RightView {
    private val projectLabel = Label("")

    private val panel = borderLayoutPanel {
        addToTop(
            vertical(
                Label("").apply { icon = Icons.TestAxis },
                Label(" "),
                Label("Welcome to TestAxis!", bold = true),
                Label("Select a build on the left to view test results."),
                Label(" "),
                projectLabel
            )
        )
    }.apply {
        border = EmptyBorder(20, 20, 20, 20)
    }

    init {
        loadProjectTitle()

        project.messageBus.connect().subscribe(
            MessageConfiguration.API_SETTINGS_UPDATED_TOPIC,
            MessageConfiguration.ApiSettingsNotifier { loadProjectTitle() }
        )
    }

    override fun getPanel() = panel

    override fun hide() {
        panel.isVisible = false
    }

    override fun show() {
        panel.isVisible = true
    }

    private fun loadProjectTitle() {
        val settings = project.service<SettingsState>()

        runBackgroundableTask("Retrieving Project Details", project, cancellable = false) {
            runBlocking {
                try {
                    val project = service<ApiService>().withProject(project).getProject(settings.projectId)
                    projectLabel.text = "Active project: ${project.slug}"
                } catch (exception: SettingsNotInitializedException) {
                    projectLabel.text = "You have not set up TestAxis yet. " +
                        "Go to Settings > Tools > TestAxis to set up your account."
                } catch (exception: UserNotAuthenticatedException) {
                    projectLabel.text = "You are not successfully authenticated. " +
                        "Go to Settings > Tools > TestAxis to set up your account."
                }
            }
        }
    }
}
