package io.testaxis.intellijplugin.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.WebsocketClient
import io.testaxis.intellijplugin.config
import io.testaxis.intellijplugin.messages.MessageConfiguration
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.settings.SettingsState

interface WebSocketService {
    val client: WebsocketClient?
}

class TestAxisWebSocketService(val project: Project) : WebSocketService {
    override var client: WebsocketClient? = loadClient()

    init {
        project.messageBus.connect().subscribe(
            MessageConfiguration.API_SETTINGS_UPDATED_TOPIC,
            MessageConfiguration.ApiSettingsNotifier {
                client?.close()
                client = loadClient()
            }
        )
    }

    @Suppress("TooGenericExceptionCaught")
    private fun loadClient() = try {
        if (project.service<SettingsState>().authenticatonToken.isNotEmpty()) {
            WebsocketClient(
                project.service<SettingsState>().serverHost,
                config(config.testaxis.ws.port),
                config(config.testaxis.ws.endpoint),
                project.service<SettingsState>().authenticatonToken
            ).apply {
                subscribe<Build>(config(config.testaxis.ws.topics.builds)) {
                    if (it.projectId == project.service<SettingsState>().projectId) {
                        project.messageBus.syncPublisher(MessageConfiguration.BUILD_FINISHED_TOPIC).notify(it)
                    }
                }
            }
        } else {
            null
        }
    } catch (exception: Exception) {
        exception.printStackTrace()
        null
    }
}
