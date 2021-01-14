package io.testaxis.intellijplugin.healthwarnings

import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.vcs.coveredFiles

class FlakinessInvestigator(private val project: Project, private val buildHistory: List<Build>) : HealthInvestigator {
    override fun investigate(testCaseExecution: TestCaseExecution, warningReporter: (String) -> Unit) {
        if (testCaseExecution.passed) {
            return
        }

        val coveredFiles = testCaseExecution.coveredFiles(project, buildHistory) ?: return

        if (coveredFiles.none { it.vcsChange != null }) {
            warningReporter(
                "This test did not fail due to code changes. The test failure may be caused by flakiness or an " +
                    "extrinsic issue such as a configuration change."
            )
        }
    }
}
