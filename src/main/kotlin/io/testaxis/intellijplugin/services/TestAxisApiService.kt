package io.testaxis.intellijplugin.services

import io.github.rybalkinsd.kohttp.dsl.async.httpGetAsync
import io.github.rybalkinsd.kohttp.ext.url
import io.github.rybalkinsd.kohttp.jackson.ext.toType
import io.testaxis.intellijplugin.Build
import io.testaxis.intellijplugin.TestCaseExecution
import io.testaxis.intellijplugin.TestCaseExecutionDetails

const val BASE_URL = "http://localhost:4000/api/v1"

class TestAxisApiService {
    class TestAxisResourceCouldNotBeRetrievedException(message: String) : Exception(message)

    suspend fun getBuilds(): List<Build> {
        val response = httpGetAsync {
            url("$BASE_URL/projects/1/builds")
        }

        return response.await().toType()
            ?: throw TestAxisResourceCouldNotBeRetrievedException("Failed to retrieve builds.")
    }

    suspend fun getTestCaseExecutions(build: Build): List<TestCaseExecution> {
        val response = httpGetAsync {
            url("$BASE_URL/projects/1/builds/${build.id}/testcaseexecutions")
        }

        return response.await().toType()
            ?: throw TestAxisResourceCouldNotBeRetrievedException("Failed to retrieve test executions for build $build")
    }

    suspend fun getTestCaseExecutionDetails(testCaseExecution: TestCaseExecution): TestCaseExecutionDetails {
        val response = httpGetAsync {
            url("$BASE_URL/projects/1/builds/0/testcaseexecutions/${testCaseExecution.id}")
        }

        return response.await().toType()
            ?: throw TestAxisResourceCouldNotBeRetrievedException("Failed to retrieve details for $testCaseExecution")
    }
}
