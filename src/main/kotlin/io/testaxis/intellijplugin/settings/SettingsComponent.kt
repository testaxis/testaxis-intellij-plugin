package io.testaxis.intellijplugin.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.Messages.showMessageDialog
import com.intellij.ui.CollectionListModel
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.Label
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.panel
import com.intellij.util.ui.UIUtil
import io.testaxis.intellijplugin.services.ApiService
import io.testaxis.intellijplugin.services.UserNotAuthenticatedException
import io.testaxis.intellijplugin.toolwindow.horizontal
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.ListSelectionModel
import io.testaxis.intellijplugin.models.Project as TestAxisProject

class SettingsComponent(val project: Project) {
    var gitHubAuthLink = HyperlinkLabel("GitHub")
    val authenticationTokenField = JBTextArea().apply {
        rows = 3
        lineWrap = true
    }
    var projectId: Int = -1
    val serverHostField = JBTextField()

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
                component(
                    horizontal(
                        JButton("Login with Email").apply {
                            addActionListener {
                                val dialog = LoginDialog(project)
                                if (dialog.showAndGet() && dialog.authenticationToken != null) {
                                    authenticationTokenField.text = dialog.authenticationToken
                                    verifyAuthenticationToken()
                                }
                            }
                        },
                        JButton("Register").apply {
                            addActionListener {
                                val dialog = RegistrationDialog(project)
                                if (dialog.showAndGet() && dialog.authenticationToken != null) {
                                    authenticationTokenField.text = dialog.authenticationToken
                                    verifyAuthenticationToken()
                                }
                            }
                        }
                    )
                )
            }
            row {
                horizontal(
                    Label("Or login through"),
                    gitHubAuthLink,
                    Label("and paste your TestAxis Authentication Token below."),
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

        hideableRow("Advanced Settings") {
            row {
                label("Server Host:")
            }
            row {
                serverHostField()
            }
            row {
                label(
                    "After changing the host, apply settings first before authentication or project selection.",
                    style = UIUtil.ComponentStyle.SMALL
                )
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

                showMessageDialog(
                    project,
                    "Welcome, ${user.name}!",
                    "Authentication Successful",
                    Messages.getInformationIcon()
                )
            } catch (exception: UserNotAuthenticatedException) {
                showMessageDialog(
                    project,
                    "Please verify that your authentication token is correct.",
                    "Authentication Unsuccessful",
                    Messages.getErrorIcon()
                )
            } catch (exception: IOException) {
                showMessageDialog(project, exception.message, "Could Not Connect To Server", Messages.getErrorIcon())
            } catch (exception: UnresolvedAddressException) {
                showMessageDialog(project, "Invalid address.", "Could Not Connect To Server", Messages.getErrorIcon())
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
        if (authenticationTokenField.text.isEmpty()) {
            return
        }

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

    fun updateGitHubAuthUrl() = gitHubAuthLink.setHyperlinkTarget(
        "https://${serverHostField.text}/oauth2/authorize/github" +
            "?redirect_uri=https://${serverHostField.text}/auth/token"
    )

    private data class ProjectOption(val project: TestAxisProject) {
        override fun toString() = project.slug
    }
}
