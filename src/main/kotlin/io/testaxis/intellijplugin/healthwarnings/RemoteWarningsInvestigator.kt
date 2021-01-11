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
                    "fails_often" -> warningReporter("This test fails often (${it.value} times in the last 50 builds).")
                    "slower_than_average" -> {
                        val average = StringUtil.formatDuration((it.value.toDouble() * 1000).toLong())
                        warningReporter("This test performs slower than average ($average).")
                    }
                }
            }
        }
    }
}
