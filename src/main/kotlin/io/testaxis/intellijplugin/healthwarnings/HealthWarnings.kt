package io.testaxis.intellijplugin.healthwarnings

import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution

fun TestCaseExecution.investigateHealth(
    project: Project,
    buildHistory: List<Build>,
    finished: () -> Unit = {},
    warningReporter: (String) -> Unit
) = runBackgroundableTask("Investigating Test Case Health", project, cancellable = false) {
    listOf(
        FlakinessInvestigator(project, buildHistory),
        RemoteWarningsInvestigator(project),
    ).forEach {
        it.investigate(this, warningReporter)
    }
    finished()
}
