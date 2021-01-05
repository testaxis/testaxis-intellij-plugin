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
import io.testaxis.intellijplugin.services.UserNotAuthenticatedException
import io.testaxis.intellijplugin.services.ValidationException
import kotlinx.coroutines.runBlocking

class LoginDialog(val project: Project) : DialogWrapper(true) {
    val email = JBTextField()
    val password = JBPasswordField()

    var authenticationToken: String? = null

    init {
        init()
        title = "Login"
    }

    override fun doOKAction() {
        val authResponse: AuthResponse
        try {
            authResponse = runBlocking {
                service<ApiService>().login(email.text, password.password.joinToString(""))
            }
        } catch (exception: ValidationException) {
            return showError(exception.message ?: "A validation error occurred")
        } catch (exception: UserNotAuthenticatedException) {
            return showError("Username or password is not correct.")
        }

        authenticationToken = authResponse.accessToken

        super.doOKAction()
    }

    override fun createCenterPanel() = panel {
        row {
            label("Email")
            email()
        }
        row {
            label("Password")
            password()
        }
    }

    private fun showError(message: String) = Messages.showMessageDialog(
        project,
        message,
        "Login Not Successful",
        Messages.getErrorIcon()
    )
}
