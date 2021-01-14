package io.testaxis.intellijplugin.healthwarnings

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.services.ApiService
import kotlinx.coroutines.runBlocking

class RemoteWarningsInvestigator(private val project: Project) : HealthInvestigator {
    override fun investigate(testCaseExecution: TestCaseExecution, warningReporter: (String) -> Unit) {
        runBlocking {
            service<ApiService>().withProject(project).getTestCaseExecutionHealth(testCaseExecution).map {
                when (it.type) {
                    "fails_often" -> warningReporter(
                        "This test is failing often (${it.value} times in the last 50 builds). This may be an " +
                            "indication that your test is too tightly coupled to your production code or that the " +
                            "test may be flaky."
                    )
                    "slower_than_average" -> {
                        val average = StringUtil.formatDuration((it.value.toDouble() * 1000).toLong())
                        warningReporter(
                            "The performance of your test suite may be improved by speeding up this test. It " +
                                "performs slower than twice the average. The average test execution time is $average."
                        )
                    }
                }
            }
        }
    }
}
