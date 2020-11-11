package io.testaxis.intellijplugin.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.intellij.openapi.components.service
import io.testaxis.intellijplugin.diffForHumans
import io.testaxis.intellijplugin.services.ApiService
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Build(
    val id: Int,
    val status: BuildStatus,
    val branch: String,
    val commit: String,
    val pr: String?,
    val service: String?,
    val serviceBuild: String?,
    val serviceBuildUrl: String?,
    val createdAt: Date,
) {
    fun label() = StringBuilder().apply {
        append("[$branch] Build for ")
        if (pr?.isNotEmpty() == true) {
            append("PR #$pr / ")
        }
        append("commit ${shortCommitHash()}")
        append(" | ${createdAt.diffForHumans()}")
    }.toString()

    fun shortCommitHash() = if (commit.length > 8) commit.subSequence(0, 8) else commit

    suspend fun retrieveTestCaseExecutions() = service<ApiService>().getTestCaseExecutions(this)
}
