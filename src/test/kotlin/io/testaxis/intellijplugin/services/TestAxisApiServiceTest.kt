package io.testaxis.intellijplugin.services

import com.intellij.openapi.components.service
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
import io.testaxis.intellijplugin.IntelliJPlatformTest
import io.testaxis.intellijplugin.config
import io.testaxis.intellijplugin.fakeBuild
import io.testaxis.intellijplugin.fakeTestCaseExecution
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.BuildStatus
import io.testaxis.intellijplugin.models.Project
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.models.TestCaseExecutionDetails
import io.testaxis.intellijplugin.models.User
import io.testaxis.intellijplugin.settings.SettingsState
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class TestAxisApiServiceTest : IntelliJPlatformTest() {
    @Test
    fun `a user can retrieve a correctly parsed user`() = runBlocking<Unit> {
        val api = createTestAxisApiService(testAxisApiUrl("/user/me")) {
            respondWithJson("user.single_success_200")
        }

        val project = api.getUser("jwt-abc-123")

        expectThat(project) isEqualTo User(
            id = 2,
            name = "Casper Boone",
            email = "testaxis@casperboone.nl",
            imageUrl = "https://avatars2.githubusercontent.com/u/15815208?v=4"
        )
    }

    @Test
    fun `a user can retrieve correctly parsed projects`() = runBlocking<Unit> {
        val api = createTestAxisApiService(testAxisApiUrl("/projects")) {
            respondWithJson("projects.all_success_200")
        }

        val projects = api.getProjects("jwt-abc-123")

        expectThat(projects).hasSize(4)
        expectThat(projects[1]) isEqualTo Project(
            id = 2,
            name = "testaxis",
            slug = "testaxis/testaxis-backend"
        )
    }

    @Test
    fun `a user can retrieve a correctly parsed project`() = runBlocking<Unit> {
        val api = createTestAxisApiService(testAxisApiUrl("/projects/2")) {
            respondWithJson("projects.single_success_200")
        }

        val project = api.getProject(2)

        expectThat(project) isEqualTo Project(
            id = 2,
            name = "testaxis",
            slug = "testaxis/testaxis-backend"
        )
    }

    @Test
    fun `a user can retrieve correctly parsed builds for a given project`() = runBlocking<Unit> {
        val api = createTestAxisApiService(testAxisApiUrl("/projects/1/builds")) {
            respondWithJson("projects.builds.all_success_200")
        }

        val builds = api.getBuilds()

        expectThat(builds).hasSize(4)
        expectThat(builds[2]) isEqualTo Build(
            id = 56,
            projectId = 2,
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
            failureContent = "io.testaxis.backend.exceptions.ResourceNo[[[OMITTED]]]Thread.java:834)\n",
            coveredLines = mapOf(
                "io.testaxis.backend.http.controllers.api.TestCaseExecutionsController" to listOf(1, 2, 3)
            )
        )
    }

    @Test
    @Disabled
    fun `a user cannot retrieve a build they do not have access to`() {
    }

    private fun testAxisApiUrl(url: String) =
        "https://${fixture.project.service<SettingsState>().serverHost}${config(config.testaxis.api.url)}$url"

    fun createTestAxisApiService(
        requestedUrl: String,
        requestHandler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): ApiService {
        fixture.project.service<SettingsState>().apply {
            authenticatonToken = "jwt-abc-123"
            projectId = 1
        }

        return TestAxisApiService(
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
        ).withProject(fixture.project)
    }

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
