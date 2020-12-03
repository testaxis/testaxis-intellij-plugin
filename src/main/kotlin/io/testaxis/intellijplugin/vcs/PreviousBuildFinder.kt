package io.testaxis.intellijplugin.vcs

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.services.GitService

private val previousBuilds = mutableMapOf<Build, Build?>()

/**
 * Finds the previous build.
 *
 * First, it retrieves a history of commit hashes up to the commit of the current build.
 * Then, it tries to find the first build that covers one of the commits from the past.
 * Since the given list of builds must be sorted, this will always be the first build
 * for the newest commit from the past.
 *
 * The result is cached in a hash map because retrieving the git history can take a bit
 * longer (but is still pretty fast).
 *
 * @param project The current project.
 * @param builds List of builds, newest first.
 * @return The previous build, if it was found. If it cannot be found, it is likely that
 *         the build commit of the current build is not present locally.
 */
fun Build.findPreviousBuild(project: Project, builds: List<Build>): Build? =
    previousBuilds.computeIfAbsent(this) {
        val commitHashesHistory = project.service<GitService>().historyUpToCommit(this.commit, ignoreErrors = true)
            .map { it.id.asString() }
            .drop(1) // first one is the build commit

        println("Commit hashes history: $commitHashesHistory")

        builds.find { commitHashesHistory.contains(it.commit) }
    }
