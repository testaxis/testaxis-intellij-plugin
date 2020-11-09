package io.testaxis.intellijplugin

import com.intellij.openapi.project.Project
import com.intellij.testFramework.registerServiceInstance
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.models.TestCaseExecutionDetails
import io.testaxis.intellijplugin.services.ApiService
import io.testaxis.intellijplugin.services.WebSocketService

data class Fakes(
    val webSocketService: WebSocketService = FakeWebSocketService(),
    val apiService: ApiService = FakeApiService()
)

fun Project.registerFakes(fakes: Fakes) {
    registerServiceInstance(WebSocketService::class.java, fakes.webSocketService)
    registerServiceInstance(ApiService::class.java, fakes.apiService)
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

class FakeApiService : ApiService {
    override suspend fun getBuilds(): List<Build> = listOf(fakeBuild(), fakeBuild(), fakeBuild())

    override suspend fun getTestCaseExecutions(build: Build): List<TestCaseExecution> =
        listOf(fakeTestCaseExecution(), fakeTestCaseExecution(), fakeTestCaseExecution())

    override suspend fun getTestCaseExecutionDetails(testCaseExecution: TestCaseExecution): TestCaseExecutionDetails =
        fakeTestCaseExecutionDetails()
}
