package io.testaxis.intellijplugin.services

import io.github.rybalkinsd.kohttp.dsl.async.httpGetAsync
import io.github.rybalkinsd.kohttp.dsl.context.HttpContext
import io.github.rybalkinsd.kohttp.ext.url
import io.github.rybalkinsd.kohttp.jackson.ext.toType
import io.testaxis.intellijplugin.config
import io.testaxis.intellijplugin.createObjectMapper
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.models.TestCaseExecutionDetails

class TestAxisApiService {
    class TestAxisResourceCouldNotBeRetrievedException(message: String) : Exception(message)

    private val objectMapper = createObjectMapper()

    private fun HttpContext.testAxisApiUrl(url: String) = url(config(config.testaxis.api.url) + url)

    suspend fun getBuilds(): List<Build> {
        val response = httpGetAsync {
            testAxisApiUrl("/projects/1/builds")
        }

        return response.await().toType(objectMapper)
            ?: throw TestAxisResourceCouldNotBeRetrievedException("Failed to retrieve builds.")
    }

    suspend fun getTestCaseExecutions(build: Build): List<TestCaseExecution> {
        val response = httpGetAsync {
            testAxisApiUrl("/projects/1/builds/${build.id}/testcaseexecutions")
        }

        return response.await().toType(objectMapper)
            ?: throw TestAxisResourceCouldNotBeRetrievedException("Failed to retrieve test executions for build $build")
    }

    suspend fun getTestCaseExecutionDetails(testCaseExecution: TestCaseExecution): TestCaseExecutionDetails {
        val response = httpGetAsync {
            testAxisApiUrl("/projects/1/builds/0/testcaseexecutions/${testCaseExecution.id}")
        }

        return response.await().toType(objectMapper)
            ?: throw TestAxisResourceCouldNotBeRetrievedException("Failed to retrieve details for $testCaseExecution")
    }
}
