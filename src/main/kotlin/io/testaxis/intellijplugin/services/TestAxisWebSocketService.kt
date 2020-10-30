package io.testaxis.intellijplugin.services

import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.WebsocketClient
import io.testaxis.intellijplugin.models.Build

class TestAxisWebSocketService(project: Project) {
    private val client = WebsocketClient("localhost", 4000)

    fun subscribeToBuilds(handler: (Build) -> Unit) = client.subscribeToBuilds(handler)
}
