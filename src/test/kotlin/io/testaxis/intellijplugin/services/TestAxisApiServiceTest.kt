package io.testaxis.intellijplugin.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.http.hostWithPort
import io.testaxis.intellijplugin.config
import io.testaxis.intellijplugin.fakeBuild
import io.testaxis.intellijplugin.fakeTestCaseExecution
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.BuildStatus
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.models.TestCaseExecutionDetails
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class TestAxisApiServiceTest {
    @Test
    fun `a user can retrieve correctly parsed builds for a given project`() = runBlocking<Unit> {
        val api = createTestAxisApiService(testAxisApiUrl("/projects/1/builds")) {
            respondWithJson("projects.builds.all_success_200")
        }

        val builds = api.getBuilds()

        expectThat(builds).hasSize(4)
        expectThat(builds[2]) isEqualTo Build(
            id = 56,
            status = BuildStatus.SUCCESS,
            branch = "upload-build-status",
            commit = "70c09adc63843c085cc10fe6bb504874c3e219bd",
            pr = "32",
            service = "github-actions",
            serviceBuild = "341979677",
            serviceBuildUrl = null,
            createdAt = builds[2].createdAt
        )
    }

    @Test
    fun `a user can retrieve correctly parsed test case executions for a given build`() = runBlocking<Unit> {
        val api = createTestAxisApiService(testAxisApiUrl("/projects/1/builds/43/testcaseexecutions")) {
            respondWithJson("projects.builds.testcaseexecutions.all_success_200")
        }

        val testCaseExecutions = api.getTestCaseExecutions(fakeBuild(id = 43))

        expectThat(testCaseExecutions).hasSize(6)
        expectThat(testCaseExecutions[2]) isEqualTo TestCaseExecution(
            id = 429,
            testSuiteName = "io.testaxis.backend.actions.ParseJUnitXMLTest",
            name = "It parses multiple XML test report with a _testsuite_ root element()",
            className = "io.testaxis.backend.actions.ParseJUnitXMLTest",
            time = 0.026,
            passed = false,
            createdAt = testCaseExecutions[2].createdAt
        )
    }

    @Test
    fun `a user can retrieve correctly parsed test case executions details`() = runBlocking<Unit> {
        val api = createTestAxisApiService(testAxisApiUrl("/projects/1/builds/0/testcaseexecutions/438")) {
            respondWithJson("projects.builds.testcaseexecutions.single_success_200")
        }

        val testCaseExecutionDetails = api.getTestCaseExecutionDetails(fakeTestCaseExecution(id = 438))

        expectThat(testCaseExecutionDetails) isEqualTo TestCaseExecutionDetails(
            failureMessage = "io.testaxis.backend.exceptions.ResourceNotFoundException",
            failureType = "io.testaxis.backend.exceptions.ResourceNotFoundException",
            failureContent = "io.testaxis.backend.exceptions.ResourceNo[[[OMITTED]]]Thread.java:834)\n"
        )
    }

    @Test
    @Disabled
    fun `a user cannot retrieve a build they do not have access to`() {
    }

    private fun testAxisApiUrl(url: String) = config(config.testaxis.api.url) + url

    fun createTestAxisApiService(
        requestedUrl: String,
        requestHandler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ) =
        TestAxisApiService(
            HttpClient(MockEngine) {
                defaultConfiguration()

                engine {
                    addHandler { request ->
                        when (request.fullUrl) {
                            requestedUrl -> requestHandler(request)
                            else -> error("Unhandled ${request.fullUrl}")
                        }
                    }
                }
            }
        )

    private val Url.fullHost: String get() = if (port == protocol.defaultPort) host else hostWithPort
    private val HttpRequestData.fullUrl: String get() = "${url.protocol.name}://${url.fullHost}${url.fullPath}"

    private fun MockRequestHandleScope.respondWithJson(
        fixtureName: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = respond(
        javaClass.getResource("/fixtures/${fixtureName.replace('.', '/')}.json").readText(),
        status,
        headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
    )
}
