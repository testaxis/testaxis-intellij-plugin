package io.testaxis.intellijplugin.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import git4idea.GitUtil
import git4idea.branch.GitBrancher
import git4idea.history.GitHistoryUtils
import git4idea.repo.GitRepository

class GitService(val project: Project) {
    val pluginCheckoutListeners = mutableListOf<() -> Unit>()

    private fun repository(): GitRepository = GitUtil.getRepositoryManager(project).repositories.apply {
        if (isEmpty()) {
            println("No git repositories found.")
            Messages.showMessageDialog(
                project,
                "No git repositories found. TestAxis only supports git projects.",
                "Git repository could not be located",
                Messages.getErrorIcon()
            )
        }
        if (size > 1) {
            println("Found more than one git repository, picking the first one.")
        }
    }.first()

    @Suppress("SpreadOperator")
    fun retrieveCommitMessages(hashes: List<String>): Map<String, String> =
        GitHistoryUtils.collectCommitsMetadata(project, repository().root, *hashes.toTypedArray())
            ?.map { it.id.asString() to it.subject }?.toMap() ?: emptyMap()

    fun currentCommit(): String? = repository().currentRevision

    fun checkout(revision: String) = GitBrancher.getInstance(project).checkout(revision, false, listOf(repository())) {
        pluginCheckoutListeners.forEach { it() }
    }
}
