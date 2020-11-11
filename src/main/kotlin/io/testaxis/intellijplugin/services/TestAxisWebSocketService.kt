package io.testaxis.intellijplugin.services

import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.WebsocketClient
import io.testaxis.intellijplugin.config
import io.testaxis.intellijplugin.models.Build

interface WebSocketService {
    val client: WebsocketClient
    fun subscribeToBuilds(handler: (Build) -> Unit)
}

class TestAxisWebSocketService(project: Project) : WebSocketService {
    override val client = WebsocketClient(
        config(config.testaxis.ws.host),
        config(config.testaxis.ws.port),
        config(config.testaxis.ws.endpoint)
    )

    override fun subscribeToBuilds(handler: (Build) -> Unit) =
        client.subscribe(config(config.testaxis.ws.topics.builds), handler)
}
