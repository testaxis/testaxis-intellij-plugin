package io.testaxis.intellijplugin.models

import io.testaxis.intellijplugin.IntelliJPlatformTest
import io.testaxis.intellijplugin.fakeBuild
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class BuildTest : IntelliJPlatformTest() {
    @Test
    fun `it can create a label to represent a build without a pr`() {
        val build = fakeBuild()

        expectThat(build.labelMaker().createString()) isEqualTo "[new-feature] Build for commit 042edd33 "
    }

    @Test
    fun `it can create a label to represent a build with a pr`() {
        val build = fakeBuild(pr = "35")

        expectThat(build.labelMaker().createString()) isEqualTo
            "[new-feature] Build for PR #35 / commit 042edd33 "
    }

    @Test
    fun `it can shorten a commit hash to be human readable`() {
        val build = fakeBuild(commit = "042edd33883d3670d131027cbc6d0543274c6152")

        expectThat(build.shortCommitHash()) isEqualTo "042edd33"
    }

    @Test
    fun `it does not shorten a commit hash that is already short`() {
        val build = fakeBuild(commit = "042")

        expectThat(build.shortCommitHash()) isEqualTo "042"
    }

    @Test
    fun `it retrieves test case executions`() {
        val build = fakeBuild(commit = "042")

        runBlocking {
            expectThat(build.retrieveTestCaseExecutions(fixture.project)) hasSize 3
        }
    }
}
