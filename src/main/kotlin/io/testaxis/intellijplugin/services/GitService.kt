package io.testaxis.intellijplugin.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.actions.diff.ShowDiffAction
import git4idea.GitCommit
import git4idea.GitUtil
import git4idea.branch.GitBrancher
import git4idea.changes.GitChangeUtils
import git4idea.history.GitHistoryUtils
import git4idea.repo.GitRepository
import io.testaxis.intellijplugin.vcs.ChangesList

interface GitService {
    val project: Project
    val pluginCheckoutListeners: MutableList<() -> Unit>

    fun retrieveCommitMessage(hash: String, ignoreErrors: Boolean = false): String?
    fun currentCommit(): String?
    fun currentBranch(): String?
    fun checkout(revision: String)
    fun changes(oldRevision: String, newRevision: String): ChangesList?
    fun historyUpToCommit(hash: String, max: Int = 100, ignoreErrors: Boolean = false): List<GitCommit>
    fun showDiff(change: Change)
}

class GitServiceImplementation(override val project: Project) : GitService {
    override val pluginCheckoutListeners = mutableListOf<() -> Unit>()

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

    override fun retrieveCommitMessage(hash: String, ignoreErrors: Boolean): String? =
        try {
            GitHistoryUtils.collectCommitsMetadata(project, repository().root, hash)?.firstOrNull()?.subject
        } catch (e: VcsException) {
            if (!ignoreErrors) {
                throw e
            }
            println("Commit message could not be retrieved for $hash: $e")
            null
        }

    override fun currentCommit(): String? = repository().currentRevision

    override fun currentBranch() = repository().currentBranch?.name

    override fun checkout(revision: String) =
        GitBrancher.getInstance(project).checkout(revision, false, listOf(repository())) {
            pluginCheckoutListeners.forEach { it() }
        }

    override fun changes(oldRevision: String, newRevision: String): ChangesList? =
        GitChangeUtils.getDiff(repository(), oldRevision, newRevision, true)?.toList()?.let { ChangesList(it) }

    override fun historyUpToCommit(hash: String, max: Int, ignoreErrors: Boolean): List<GitCommit> =
        try {
            repository().let { repo ->
                GitHistoryUtils.history(project, repo.root, hash, "--max-count=$max")
            }
        } catch (e: VcsException) {
            if (!ignoreErrors) {
                throw e
            }
            println("History could not be retrieved for $hash: $e")
            emptyList()
        }

    override fun showDiff(change: Change) =
        ShowDiffAction.showDiffForChange(project, listOf(change))
}
