package io.testaxis.intellijplugin.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.serviceContainer.NonInjectable
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.testaxis.intellijplugin.config
import io.testaxis.intellijplugin.createObjectMapper
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.HealthWarning
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.models.TestCaseExecutionDetails
import io.testaxis.intellijplugin.models.User
import io.testaxis.intellijplugin.settings.SettingsState
import io.testaxis.intellijplugin.models.Project as TestAxisProject

interface ApiService {
    suspend fun login(email: String, password: String): AuthResponse

    suspend fun registerUser(name: String, email: String, password: String): AuthResponse

    suspend fun getUser(authenticationToken: String): User

    suspend fun getProjects(authenticationToken: String): List<TestAxisProject>

    suspend fun getProject(id: Int): TestAxisProject

    suspend fun getBuilds(): List<Build>

    suspend fun getTestCaseExecutions(build: Build): List<TestCaseExecution>

    suspend fun getTestCaseExecutionDetails(testCaseExecution: TestCaseExecution): TestCaseExecutionDetails

    suspend fun getTestCaseExecutionHealth(testCaseExecution: TestCaseExecution): List<HealthWarning>

    fun withProject(project: Project): ApiService
}

@Suppress("TooManyFunctions")
class TestAxisApiService @NonInjectable constructor(val client: HttpClient = defaultClient()) : ApiService, Disposable {
    private lateinit var project: Project

    private fun testAxisApiUrl(url: String) = config(config.testaxis.api.url) + url

    override suspend fun login(email: String, password: String): AuthResponse = catch {
        client.post(testAxisApiUrl("/auth/login")) {
            body = TextContent(
                createObjectMapper().writeValueAsString(mapOf("email" to email, "password" to password)),
                contentType = ContentType.Application.Json
            )
        }
    }

    override suspend fun registerUser(name: String, email: String, password: String): AuthResponse = catch {
        client.post(testAxisApiUrl("/auth/register")) {
            body = TextContent(
                createObjectMapper().writeValueAsString(
                    mapOf(
                        "name" to name,
                        "email" to email,
                        "password" to password
                    )
                ),
                contentType = ContentType.Application.Json
            )
        }
    }

    override suspend fun getUser(authenticationToken: String): User =
        catch { client.get(testAxisApiUrl("/user/me")) { bearerAuthorization(authenticationToken) } }

    override suspend fun getProjects(authenticationToken: String): List<TestAxisProject> =
        catch { client.get(testAxisApiUrl("/projects")) { bearerAuthorization(authenticationToken) } }

    override suspend fun getProject(id: Int): TestAxisProject =
        catch { client.get(testAxisApiUrl("/projects/$id")) { bearerAuthorization(settings().authenticatonToken) } }

    override suspend fun getBuilds(): List<Build> = catch {
        client.get(testAxisApiUrl("/projects/${settings().projectId}/builds")) {
            bearerAuthorization(settings().authenticatonToken)
        }
    }

    override suspend fun getTestCaseExecutions(build: Build): List<TestCaseExecution> = catch {
        client.get(testAxisApiUrl("/projects/${settings().projectId}/builds/${build.id}/testcaseexecutions")) {
            bearerAuthorization(settings().authenticatonToken)
        }
    }

    override suspend fun getTestCaseExecutionDetails(testCaseExecution: TestCaseExecution): TestCaseExecutionDetails =
        catch {
            val url = "/projects/${settings().projectId}/builds/${testCaseExecution.build?.id ?: 0}" +
                "/testcaseexecutions/${testCaseExecution.id}"
            client.get(testAxisApiUrl(url)) { bearerAuthorization(settings().authenticatonToken) }
        }

    override suspend fun getTestCaseExecutionHealth(testCaseExecution: TestCaseExecution): List<HealthWarning> =
        catch {
            val url = "/projects/${settings().projectId}/builds/${testCaseExecution.build?.id ?: 0}" +
                "/testcaseexecutions/${testCaseExecution.id}/health"
            client.get(testAxisApiUrl(url)) { bearerAuthorization(settings().authenticatonToken) }
        }

    override fun withProject(project: Project): ApiService {
        this.project = project

        return this
    }

    override fun dispose() {
        client.close()
    }

    private fun settings(): SettingsState {
        if (!this::project.isInitialized) {
            error("Cannot get settings, project not provided.")
        }
        val settings = project.service<SettingsState>()
        if (!settings.isInitialized()) {
            throw SettingsNotInitializedException()
        }
        return settings
    }
}

private fun defaultClient() = HttpClient { defaultConfiguration() }

internal fun HttpClientConfig<*>.defaultConfiguration() {
    install(JsonFeature) {
        serializer = JacksonSerializer(createObjectMapper())
    }
}

private fun HttpRequestBuilder.bearerAuthorization(token: String) = header("Authorization", "Bearer $token")

private suspend fun <R> catch(action: suspend () -> R) =
    try {
        action()
    } catch (exception: ClientRequestException) {
        if (exception.response.status.value == 401) {
            throw UserNotAuthenticatedException(exception)
        }
        if (exception.response.status.value == 422) {
            throw ValidationException(exception)
        }
        throw exception
    }

class SettingsNotInitializedException : Exception("There is no project selected in the settings.")

class UserNotAuthenticatedException(exception: ClientRequestException) :
    Exception("The user is not (properly) authenticated.", exception)

class ValidationException(exception: ClientRequestException) :
    Exception("A validation error occurred.", exception)

data class AuthResponse(val accessToken: String, val tokenType: String)
