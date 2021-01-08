package io.testaxis.intellijplugin.vcs

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.services.PsiService
import kotlinx.coroutines.runBlocking

private val coveredFilesCache = mutableMapOf<TestCaseExecution, List<CoveredFile>?>()

fun TestCaseExecution.coveredFiles(project: Project, buildHistory: List<Build>): List<CoveredFile>? =
    coveredFilesCache.computeIfAbsent(this) {
        val previousBuild = build?.findPreviousBuild(project, buildHistory) ?: return@computeIfAbsent null

        val changes = build?.changes(project, previousBuild)

        runBlocking {
            details(project)
                .coveredLines
                .map { (fileName, lines) -> CoveredFile(fileName, lines, changes?.changeForPartialFileName(fileName)) }
                .sortedBy { it.vcsChange?.type?.ordinal ?: Int.MAX_VALUE }
        }
    }

data class CoveredFile(val fileName: String, val lines: List<Int>, val vcsChange: Change?) {
    fun getFile(project: Project) = project.service<PsiService>().findFileByRelativePath(fileName)

    fun name() = fileName.substringAfterLast('/').substringBeforeLast('.')
}
