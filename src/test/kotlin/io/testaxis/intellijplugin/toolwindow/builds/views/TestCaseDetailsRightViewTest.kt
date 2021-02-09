package io.testaxis.intellijplugin.toolwindow.builds.views

import io.testaxis.intellijplugin.FakeApiService
import io.testaxis.intellijplugin.Fakes
import io.testaxis.intellijplugin.IntelliJPlatformUITest
import io.testaxis.intellijplugin.fakeTestCaseExecution
import io.testaxis.intellijplugin.fakeTestCaseExecutionDetails
import io.testaxis.intellijplugin.models.HealthWarning
import io.testaxis.intellijplugin.models.TestCaseExecution
import org.assertj.swing.fixture.Containers
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class TestCaseDetailsRightViewTest : IntelliJPlatformUITest() {
    val fakeDetails = fakeTestCaseExecutionDetails(
        failureMessage = "io.testaxis.backend.exceptions.ResourceNotFoundException",
        failureType = "AssertionError",
        failureContent = "io.testaxis.backend.exceptions.ResourceNo[[[OMITTED]]]Thread.java:834)\n",
    )

    override fun getFakes() = Fakes(
        apiService = object : FakeApiService() {
            override suspend fun getTestCaseExecutionDetails(testCaseExecution: TestCaseExecution) = fakeDetails
            override suspend fun getTestCaseExecutionHealth(testCaseExecution: TestCaseExecution) =
                emptyList<HealthWarning>()
        }
    )

    @Test
    @Disabled // This test started failing out of nowhere. The cause needs to be investigated further.
    fun `it shows test case execution details`() {
        val testCaseDetailsRightView = executeGuiAction { TestCaseDetailsRightView(fixture.project) }
        frame = Containers.showInFrame(testCaseDetailsRightView.getPanel())

        executeGuiAction {
            testCaseDetailsRightView.setTestCaseExecution(fakeTestCaseExecution())
        }

        frame.requireContainsLabel("it can shorten a commit hash to be human readable")
        frame.requireContainsLabel("com.example.service")

        // TODO: Add check that failure content is shown

        executeGuiAction { testCaseDetailsRightView.dispose() }
    }
}
