package io.testaxis.intellijplugin.services

import com.intellij.diff.comparison.ComparisonManager
import com.intellij.diff.comparison.ComparisonPolicy
import com.intellij.diff.fragments.LineFragment
import com.intellij.openapi.progress.DumbProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.changes.Change
import git4idea.GitUtil
import git4idea.branch.GitBrancher
import git4idea.changes.GitChangeUtils
import git4idea.history.GitHistoryUtils
import git4idea.repo.GitRepository
import io.testaxis.intellijplugin.gitchanches.ChangesList
import java.io.File

interface GitService {
    val project: Project
    val pluginCheckoutListeners: MutableList<() -> Unit>

    fun retrieveCommitMessages(hashes: List<String>): Map<String, String>
    fun currentCommit(): String?
    fun checkout(revision: String)
    fun changes(oldRevision: String, newRevision: String): ChangesList?
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

    @Suppress("SpreadOperator")
    override fun retrieveCommitMessages(hashes: List<String>): Map<String, String> =
        GitHistoryUtils.collectCommitsMetadata(project, repository().root, *hashes.toTypedArray())
            ?.map { it.id.asString() to it.subject }?.toMap() ?: emptyMap()

    override fun currentCommit(): String? = repository().currentRevision

    override fun checkout(revision: String) =
        GitBrancher.getInstance(project).checkout(revision, false, listOf(repository())) {
            pluginCheckoutListeners.forEach { it() }
        }

    override fun changes(oldRevision: String, newRevision: String): ChangesList? =
        GitChangeUtils.getDiff(repository(), oldRevision, newRevision,true)?.toList()?.let { ChangesList(it) }

    // Replace with a map of changes to the changed file list. ChangedFile class should be top-level and have behavior
    // to get the textual diff and information about the type etc. Hm basically that becomes just the `Change` class with diff functionality.
    // Maybe add the diff as an extension function and then we're done already
//        changes?.forEach {
//
//            when (it.type) {
//                Change.Type.NEW -> {
//                    // TODO: Add implementation
//                }
//                Change.Type.DELETED -> {
//                    // TODO: Add implementation
//                }
//                Change.Type.MOVED -> {
//                    // TODO: Add implementation
//                }
//                Change.Type.MODIFICATION -> {
////                        virtualFile?.toNioPath() // if it does not exit the after revision does not exist and we don't care
//
//                    val beforeRevision = it.beforeRevision
//                    val afterRevision = it.afterRevision
//
//                    if (beforeRevision != null && afterRevision != null) {
//                        val path = afterRevision.file.path
//
//                        // assume newest commits are first, so always replace the oldest revision, but keep the newest one
//                        changedFiles[path] = ChangedFile(beforeRevision, changedFiles[path]?.after ?: afterRevision)
//                    }
//                }
//            }
//        }
//
//        changedFiles.forEach { (t, u) -> println("$t -- {${u.before.revisionNumber} - ${u.after.revisionNumber}") }
}

//
//    override fun getChanges() {
//        val repo = repository()
////        val committedChangesProvider = repo.vcs.committedChangesProvider ?: return
////
////        val filePath = LocalFilePath(repo.root.path, repo.root.isDirectory)
////
////        val settings = committedChangesProvider.createDefaultSettings().apply {
////            USE_CHANGE_AFTER_FILTER = true
////            USE_CHANGE_BEFORE_FILTER = true
////            CHANGE_AFTER = GitChangeUtils.longForSHAHash("faa2838d484e2bd8caa93b4e46865bbf915dd76c").toString()
////            CHANGE_BEFORE = GitChangeUtils.longForSHAHash("71d660f3af6d71bcfd53b07c5e1c2ed48f57d81c").toString()
////        }
////
////        println("Settings: before filter: ${settings.changeBeforeFilter}, after filter: ${settings.changeAfterFilter}")
////
////
////        val revisions: List<CommittedChangeList> = committedChangesProvider.getCommittedChanges(
////            settings, // via the settings we can set start/end commit etc
////            GitRepositoryLocation(repo.root.url, File(repo.root.toNioPath().toUri())),
////            committedChangesProvider.unlimitedCountValue
////        )
//
//        data class ChangedFile(val before: ContentRevision, val after: ContentRevision)
//
//        val changedFiles = mutableMapOf<String, ChangedFile>()
////
////        println("Amount of revisions: ${revisions.count()}")
////        println("Revisions: ${revisions.map { it.name }}")
//
//
//        val changes = GitChangeUtils.getDiff(
//            repo,
//            "1f56fdfe87f201723e6ef3ffe44f652cc1232ab1",
//            "71d660f3af6d71bcfd53b07c5e1c2ed48f57d81c",
//            true
//        )
//
////        revisions.forEach { revision ->
////            revision.changes.forEach {
//            changes?.forEach {
//
//                when (it.type) {
//                    Change.Type.NEW -> {
//                        // TODO: Add implementation
//                    }
//                    Change.Type.DELETED -> {
//                        // TODO: Add implementation
//                    }
//                    Change.Type.MOVED -> {
//                        // TODO: Add implementation
//                    }
//                    Change.Type.MODIFICATION -> {
////                        virtualFile?.toNioPath() // if it does not exit the after revision does not exist and we don't care
//
//                        val beforeRevision = it.beforeRevision
//                        val afterRevision = it.afterRevision
//
//                        if (beforeRevision != null && afterRevision != null) {
//                            val path = afterRevision.file.path
//
//                            // assume newest commits are first, so always replace the oldest revision, but keep the newest one
//                            changedFiles[path] = ChangedFile(beforeRevision, changedFiles[path]?.after ?: afterRevision)
//                        }
//                    }
//                }
////            }
//        }
//
////        val b: CommittedChangeList = (a as List<CommittedChangeList>).first()
//
////                b.changesWithMovedTrees
////        b.changes.first().afterRevision
//
//        // changes.first().beforeRevision can already be used to determine _if_ a file has been changed
//        // in the commit
//
//        // option 1 diff changes.first().beforeRevision and changes.first().afterRevision manually
//
////                val compare = ComparisonManager.getInstance().compareLines(
////                    b.changes.toList()[2].beforeRevision?.content ?: throw IllegalStateException(),
////                    b.changes.toList()[2].afterRevision?.content ?: throw IllegalStateException(),
////                    ComparisonPolicy.DEFAULT,
////                    DumbProgressIndicator.INSTANCE
////                )
//
////       THIS ONE WORKS!!
////        val compare = ComparisonManager.getInstance().compareLines(
////            """
////                        hallo
////                        deze gaat weg
////                        goedenavond
////                    """.trimIndent(),
////            """
////                        hallo
////                        goedenavond
////                    """.trimIndent(),
////            ComparisonPolicy.DEFAULT,
////            DumbProgressIndicator.INSTANCE
////        )
//
//        // compare.myStartLine2 till (but not including!) compare.myEndLine2 show where the change is in the new file
//        // This makes at least sense for insertions/modifications.
//        // For deletions the myStartLine2 and myEndLine2 are the same. We could ignore this or highlight this somehow
//
//        // Also important: how does this work with new files (answer: probably the beforeRevision is null)
//        // Also important: how does this work with deleted files (answer: probably the afterRevision is null)
//
//        println(changedFiles)
//    }
//}
