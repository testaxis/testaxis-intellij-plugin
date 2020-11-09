package io.testaxis.intellijplugin.models

import io.testaxis.intellijplugin.FakeApiService
import io.testaxis.intellijplugin.Fakes
import io.testaxis.intellijplugin.IntelliJPlatformTest
import io.testaxis.intellijplugin.fakeTestCaseExecution
import io.testaxis.intellijplugin.fakeTestCaseExecutionDetails
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isSameInstanceAs

class TestCaseExecutionTest : IntelliJPlatformTest() {
    val fakeDetails = fakeTestCaseExecutionDetails()

    override fun getFakes() = Fakes(
        apiService = object : FakeApiService() {
            override suspend fun getTestCaseExecutionDetails(testCaseExecution: TestCaseExecution) = fakeDetails
        }
    )

    @Test
    fun `it can retrieve the execution details from the api service`() {
        val testCaseExecution = fakeTestCaseExecution()

        runBlocking {
            expectThat(testCaseExecution.details()) isSameInstanceAs fakeDetails
        }
    }
}
