package io.testaxis.intellijplugin.vcs

import com.intellij.diff.comparison.ComparisonManager
import com.intellij.diff.comparison.ComparisonPolicy
import com.intellij.diff.fragments.LineFragment
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.DumbProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.services.GitService
import java.io.File

private val changesForBuilds = mutableMapOf<Build, ChangesList?>()

fun Build.changes(project: Project, previousBuild: Build): ChangesList {
    if (!changesForBuilds.containsKey(this)) {
        changesForBuilds[this] = project.service<GitService>().changes(previousBuild.commit, commit)
    }
    return changesForBuilds[this] ?: error("List of changes is not present.")
}

data class ChangesList(val changes: List<Change>) {
    fun changeForFile(file: File) = changes
        .filter { it.affectsFile(file) }
        .also { if (it.size > 1) println("There is more than one change for file ${file.path}.") }
        .firstOrNull()

    fun changeForPartialFileName(fileName: String) = changes
        .filter { it.afterRevision?.file?.path?.endsWith(fileName) ?: false }
        .sortedBy { it.afterRevision?.file?.path?.length }
        .also { if (it.size > 1) println("There is more than one change for file $fileName.") }
        .firstOrNull()
}

typealias TextualDiff = List<LineFragment>

private val textualDiffCache = mutableMapOf<Change, TextualDiff>()

fun Change.textualDiff(): TextualDiff {
    if (!textualDiffCache.containsKey(this)) {
        textualDiffCache[this] = ComparisonManager.getInstance().compareLinesInner(
            beforeRevision?.content ?: error("Diff can only be applied to modified files."),
            afterRevision?.content ?: error("Diff can only be applied to modified files."),
            ComparisonPolicy.DEFAULT,
            DumbProgressIndicator.INSTANCE
        )
    }
    return textualDiffCache[this]!! // The null-assertion is safe because we never remove items from the map
}

fun TextualDiff.deletions() = sumBy { line ->
    if (line.innerFragments == null) {
        if (line.startOffset2 == line.endOffset2) 1 else 0
    } else {
        line.innerFragments?.count { it.startOffset2 == it.endOffset2 } ?: 0
    }
}

fun TextualDiff.changedLines() = this
    .map { fragment -> ((fragment.startLine2 + 1) until (fragment.endLine2 + 1)) }
    .flatMap { range -> range.toList() }
