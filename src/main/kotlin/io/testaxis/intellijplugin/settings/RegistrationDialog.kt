package io.testaxis.intellijplugin.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import io.testaxis.intellijplugin.services.ApiService
import io.testaxis.intellijplugin.services.AuthResponse
import io.testaxis.intellijplugin.services.ValidationException
import kotlinx.coroutines.runBlocking

class RegistrationDialog(val project: Project) : DialogWrapper(true) {
    val name = JBTextField()
    val email = JBTextField()
    val password = JBPasswordField()
    val passwordConfirmation = JBPasswordField()

    var authenticationToken: String? = null

    init {
        init()
        title = "Register New Account"
    }

    override fun doOKAction() {
        if (password.password.joinToString("") != passwordConfirmation.password.joinToString("")) {
            return showError("The passwords do not match.")
        }

        val authResponse: AuthResponse
        try {
            authResponse = runBlocking {
                project.service<ApiService>().registerUser(name.text, email.text, password.password.joinToString(""))
            }
        } catch (exception: ValidationException) {
            return showError(exception.message ?: "A validation error occurred")
        }

        authenticationToken = authResponse.accessToken

        super.doOKAction()
    }

    override fun createCenterPanel() = panel {
        row {
            label("Name")
            name()
        }
        row {
            label("Email")
            email()
        }
        row {
            label("Password")
            password()
        }
        row {
            label("Confirm Password")
            passwordConfirmation()
        }
    }

    private fun showError(message: String) = Messages.showMessageDialog(
        project,
        message,
        "User Could Not Be Registered",
        Messages.getErrorIcon()
    )
}
