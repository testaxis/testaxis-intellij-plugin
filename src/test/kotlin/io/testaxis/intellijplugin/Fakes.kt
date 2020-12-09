package io.testaxis.intellijplugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.testFramework.registerServiceInstance
import git4idea.GitCommit
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.models.TestCaseExecutionDetails
import io.testaxis.intellijplugin.services.ApiService
import io.testaxis.intellijplugin.services.GitService
import io.testaxis.intellijplugin.services.WebSocketService
import io.testaxis.intellijplugin.vcs.ChangesList

data class Fakes(
    val webSocketService: WebSocketService = FakeWebSocketService(),
    val apiService: ApiService = FakeApiService(),
    val gitService: GitService = FakeGitService()
) : Disposable {
    override fun dispose() {}
}

fun Project.registerFakes(fakes: Fakes) {
    registerServiceInstance(WebSocketService::class.java, fakes.webSocketService)
    registerServiceInstance(GitService::class.java, fakes.gitService)

    ApplicationManager.getApplication().registerServiceInstance(ApiService::class.java, fakes.apiService)
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

open class FakeApiService : ApiService {
    override suspend fun getBuilds(): List<Build> = listOf(fakeBuild(), fakeBuild(), fakeBuild())

    override suspend fun getTestCaseExecutions(build: Build): List<TestCaseExecution> =
        listOf(fakeTestCaseExecution(), fakeTestCaseExecution(), fakeTestCaseExecution())

    override suspend fun getTestCaseExecutionDetails(testCaseExecution: TestCaseExecution): TestCaseExecutionDetails =
        fakeTestCaseExecutionDetails()
}

class FakeGitService : GitService {
    override val project: Project
        get() = TODO("Not yet implemented")

    override val pluginCheckoutListeners = mutableListOf<() -> Unit>()

    override fun retrieveCommitMessage(hash: String, ignoreErrors: Boolean): String? = null

    override fun currentCommit(): String? = null

    override fun currentBranch(): String? = null

    override fun checkout(revision: String) {
        TODO("Not yet implemented")
    }

    override fun changes(oldRevision: String, newRevision: String): ChangesList? {
        TODO("Not yet implemented")
    }

    override fun historyUpToCommit(hash: String, max: Int, ignoreErrors: Boolean): List<GitCommit> {
        TODO("Not yet implemented")
    }

    override fun showDiff(change: Change) {
        TODO("Not yet implemented")
    }
}
