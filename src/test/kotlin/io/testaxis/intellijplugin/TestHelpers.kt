package io.testaxis.intellijplugin

import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.runInEdtAndWait
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.BuildStatus
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
    createdAt: Date = Date()
) = Build(id, status, branch, commit, pr, service, serviceBuild, serviceBuildUrl, createdAt)
