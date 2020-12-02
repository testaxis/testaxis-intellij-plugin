package io.testaxis.intellijplugin.services

import com.intellij.openapi.Disposable
import com.intellij.serviceContainer.NonInjectable
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.testaxis.intellijplugin.config
import io.testaxis.intellijplugin.createObjectMapper
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.models.TestCaseExecutionDetails

interface ApiService {
    suspend fun getBuilds(): List<Build>

    suspend fun getTestCaseExecutions(build: Build): List<TestCaseExecution>

    suspend fun getTestCaseExecutionDetails(testCaseExecution: TestCaseExecution): TestCaseExecutionDetails
}

class TestAxisApiService @NonInjectable constructor(val client: HttpClient = defaultClient()) : ApiService, Disposable {
    private fun testAxisApiUrl(url: String) = config(config.testaxis.api.url) + url

    override suspend fun getBuilds(): List<Build> =
        client.get<List<Build>>(testAxisApiUrl("/projects/1/builds")).also { builds ->
            builds.sortedBy { it.id }.windowed(size = 2).forEach { it[1].previousBuild = it[0] }
            // TODO: Needs better logic to determine the previous build
        }

    override suspend fun getTestCaseExecutions(build: Build): List<TestCaseExecution> =
        client.get(testAxisApiUrl("/projects/1/builds/${build.id}/testcaseexecutions"))

    override suspend fun getTestCaseExecutionDetails(testCaseExecution: TestCaseExecution): TestCaseExecutionDetails =
        client.get(testAxisApiUrl("/projects/1/builds/0/testcaseexecutions/${testCaseExecution.id}"))

    override fun dispose() {
        client.close()
    }
}

private fun defaultClient() = HttpClient { defaultConfiguration() }

internal fun HttpClientConfig<*>.defaultConfiguration() {
    install(JsonFeature) {
        serializer = JacksonSerializer(createObjectMapper())
    }
}
