package io.testaxis.intellijplugin.healthwarnings

import io.testaxis.intellijplugin.models.TestCaseExecution

interface HealthInvestigator {
    fun investigate(testCaseExecution: TestCaseExecution, warningReporter: (String) -> Unit)
}
