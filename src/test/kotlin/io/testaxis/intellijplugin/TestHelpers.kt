package io.testaxis.intellijplugin

import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.runInEdtAndWait
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.BuildStatus
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.models.TestCaseExecutionDetails
import java.util.Calendar
import java.util.Date

fun createDescriptor(setUpProjectInstructor: Project.() -> Unit) = object : LightProjectDescriptor() {
    override fun setUpProject(project: Project, handler: SetupHandler) {
        project.setUpProjectInstructor()

        super.setUpProject(project, handler)
    }
}

fun createFixture(descriptor: LightProjectDescriptor) =
    IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder(descriptor).fixture.also {
        runInEdtAndWait {
            it.setUp()
        }
    }

fun IdeaTestFixture.tearDownInEdt() {
    runInEdtAndWait {
        tearDown()
    }
}

fun fakeBuild(
    id: Int = 1,
    status: BuildStatus = BuildStatus.SUCCESS,
    branch: String = "new-feature",
    commit: String = "042edd33883d3670d131027cbc6d0543274c6152",
    pr: String? = null,
    service: String? = null,
    serviceBuild: String? = null,
    serviceBuildUrl: String? = null,
    createdAt: Date = aFewSecondsAgo()
) = Build(id, status, branch, commit, pr, service, serviceBuild, serviceBuildUrl, createdAt)

fun fakeTestCaseExecution(
    id: Int = 1,
    testSuiteName: String = "com.example.service",
    name: String = "it can shorten a commit hash to be human readable",
    className: String = "com.example.service",
    time: Double = 0.35,
    passed: Boolean = true,
    createdAt: Date = aFewSecondsAgo(),
) = TestCaseExecution(id, testSuiteName, name, className, time, passed, createdAt)

fun fakeTestCaseExecutionDetails(
    failureMessage: String? = null,
    failureType: String? = null,
    failureContent: String? = null,
    coveredLines: Map<String, List<Int>> = emptyMap(),
) = TestCaseExecutionDetails(failureMessage, failureType, failureContent, coveredLines)

fun aFewSecondsAgo(seconds: Int = 1): Date = Calendar.getInstance().apply {
    set(Calendar.SECOND, get(Calendar.SECOND) - seconds)
}.time
