package io.testaxis.intellijplugin.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.messages.MessageConfiguration
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class SettingsConfigurable(val project: Project) : Configurable {
    private lateinit var settingsComponent: SettingsComponent

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName() = "TestAxis"

    override fun createComponent(): JComponent {
        settingsComponent = SettingsComponent(project)

        return settingsComponent.panel
    }

    override fun isModified(): Boolean {
        val settings = project.service<SettingsState>()

        return settingsComponent.authenticationTokenField.text != settings.authenticatonToken ||
            settingsComponent.projectId != settings.projectId ||
            settingsComponent.serverHostField.text != settings.serverHost
    }

    override fun apply() {
        val settings = project.service<SettingsState>()

        settings.authenticatonToken = settingsComponent.authenticationTokenField.text
        settings.projectId = settingsComponent.projectId
        settings.serverHost = settingsComponent.serverHostField.text

        project.messageBus.syncPublisher(MessageConfiguration.API_SETTINGS_UPDATED_TOPIC).notify(settings)
    }

    override fun reset() {
        val settings = project.service<SettingsState>()

        settingsComponent.authenticationTokenField.text = settings.authenticatonToken
        settingsComponent.projectId = settings.projectId
        settingsComponent.serverHostField.text = settings.serverHost

        settingsComponent.loadProjectsIfEmpty()
    }
}
