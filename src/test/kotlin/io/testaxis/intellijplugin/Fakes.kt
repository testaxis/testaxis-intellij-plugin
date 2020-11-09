package io.testaxis.intellijplugin

import com.intellij.openapi.project.Project
import com.intellij.testFramework.registerServiceInstance
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.services.WebSocketService

data class Fakes(
    val webSocketService: WebSocketService = FakeWebSocketService(),
)

fun Project.registerFakes(fakes: Fakes) {
    registerServiceInstance(WebSocketService::class.java, fakes.webSocketService)
}

class FakeWebSocketService : WebSocketService {
    override val client: WebsocketClient
        get() = TODO("Not yet implemented")

    private val buildHandlers = mutableListOf<(Build) -> Unit>()

    override fun subscribeToBuilds(handler: (Build) -> Unit) {
        buildHandlers.add(handler)
    }

    fun reportNewBuild(build: Build) = buildHandlers.forEach { it(build) }
}
