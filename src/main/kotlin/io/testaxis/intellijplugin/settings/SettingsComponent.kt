package io.testaxis.intellijplugin.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.CollectionListModel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.Label
import com.intellij.ui.components.htmlComponent
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.panel
import io.testaxis.intellijplugin.config
import io.testaxis.intellijplugin.services.ApiService
import io.testaxis.intellijplugin.services.UserNotAuthenticatedException
import kotlinx.coroutines.runBlocking
import javax.swing.BorderFactory
import javax.swing.ListSelectionModel
import io.testaxis.intellijplugin.models.Project as TestAxisProject

class SettingsComponent(val project: Project) {
    val authenticationTokenField = JBTextArea().apply {
        rows = 3
        lineWrap = true
    }
    var projectId: Int = -1

    private var userNameLabel = Label("", bold = true)
    private var userEmailLabel = Label("")

    private val projectsList = JBList(CollectionListModel<ProjectOption>()).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        border = BorderFactory.createLineBorder(JBColor.border())
        addListSelectionListener {
            if (selectedValue != null) {
                projectId = selectedValue.project.id
            }
        }
    }

    val panel = panel {
        titledRow("Authentication Token") {
            row {
                htmlComponent(
                    "Login through <a href=\"${config(config.testaxis.auth.githubUrl)}\">GitHub</a> " +
                        "and paste your TestAxis Authentication Token below."
                )()
            }
            row {
                authenticationTokenField().focused().growPolicy(GrowPolicy.MEDIUM_TEXT)
                cell(isVerticalFlow = true) {
                    button("Verify") { verifyAuthenticationToken() }
                    userNameLabel()
                    userEmailLabel()
                }
            }
        }

        titledRow("Select Project") {
            row {
                projectsList(growX, pushX)
            }
            row {
                commentRow("Verify a new authentication token to refresh the list of projects.")
            }
        }
    }

    init {
        loadProjects()
    }

    private fun verifyAuthenticationToken() {
        runBlocking {
            try {
                val user = service<ApiService>().getUser(authenticationTokenField.text)

                userNameLabel.text = user.name
                userEmailLabel.text = user.email

                loadProjects()

                Messages.showMessageDialog(
                    project,
                    "Welcome, ${user.name}!",
                    "Authentication Successful",
                    Messages.getInformationIcon()
                )
            } catch (exception: UserNotAuthenticatedException) {
                Messages.showMessageDialog(
                    project,
                    "Please verify that your authentication token is correct.",
                    "Authentication Unsuccessful",
                    Messages.getErrorIcon()
                )
            }
        }
    }

    fun loadProjectsIfEmpty() {
        val model = projectsList.model as CollectionListModel

        if (model.isEmpty) {
            loadProjects()
        }
    }

    private fun loadProjects() {
        runBackgroundableTask("Retrieving Projects", project, cancellable = false) {
            runBlocking {
                val projectOptions = service<ApiService>().getProjects(authenticationTokenField.text).map {
                    ProjectOption(it)
                }
                projectsList.model = CollectionListModel(projectOptions)
                projectsList.setSelectedValue(projectOptions.find { it.project.id == projectId }, true)
            }
        }
    }

    private data class ProjectOption(val project: TestAxisProject) {
        override fun toString() = project.slug
    }
}
