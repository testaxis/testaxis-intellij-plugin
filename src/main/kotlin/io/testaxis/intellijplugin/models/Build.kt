package io.testaxis.intellijplugin.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.intellij.openapi.components.service
import com.intellij.ui.SimpleTextAttributes
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
    var commitMessage: String? = null

    fun shortCommitHash() = if (commit.length > 8) commit.subSequence(0, 8) else commit

    fun labelMaker() = BuildLabelMaker(this)

    suspend fun retrieveTestCaseExecutions() =
        service<ApiService>().getTestCaseExecutions(this).onEach { it.build = this }
}

class BuildLabelMaker(val build: Build) {
    private var includeCreatedAt: Boolean = false
    private var includeCommitMessage: Boolean = false

    fun withCreatedAt(): BuildLabelMaker {
        includeCreatedAt = true

        return this
    }

    fun withCommitMessage(): BuildLabelMaker {
        includeCommitMessage = true

        return this
    }

    data class Item(val text: String, val attributes: SimpleTextAttributes)

    fun createItems() = listOfNotNull(
        Item("[${build.branch}] ", SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES),
        Item("Build for ", SimpleTextAttributes.REGULAR_ATTRIBUTES),
        ifEnabled(build.pr?.isNotEmpty()) {
            Item("PR #${build.pr} / ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        },
        Item("commit ${build.shortCommitHash()} ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES),
        ifEnabled(includeCommitMessage && build.commitMessage != null) {
            Item("${build.commitMessage} ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        },
        ifEnabled(includeCreatedAt) {
            Item("| ${build.createdAt.diffForHumans()}", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        }
    )

    fun createString() = createItems().joinToString("") { it.text }

    private fun ifEnabled(condition: Boolean?, fragmentProvider: () -> Item) =
        if (condition == true) fragmentProvider() else null
}
