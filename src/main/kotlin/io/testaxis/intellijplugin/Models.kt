package io.testaxis.intellijplugin

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.intellij.openapi.components.ServiceManager
import io.testaxis.intellijplugin.services.TestAxisApiService
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
data class Build(
    val id: Int,
    val branch: String,
    val commit: String,
    val pr: String?,
    val service: String?,
    val serviceBuild: String?,
    val serviceBuildUrl: String?,
    val createdAt: Date,
) {
    fun label() = "[$branch] Build for PR #$pr / commit $commit"

    suspend fun retrieveTestCaseExecutions() =
        ServiceManager.getService(TestAxisApiService::class.java).getTestCaseExecutions(this)
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class TestCaseExecution(
    val id: Int,
    val testSuiteName: String,
    val name: String,
    val className: String,
    val time: Double,
    val passed: Boolean,
    val createdAt: Date
) {
    suspend fun details() =
        ServiceManager.getService(TestAxisApiService::class.java).getTestCaseExecutionDetails(this)
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class TestCaseExecutionDetails(
    val id: Int,
    val testSuiteName: String,
    val name: String,
    val className: String,
    val time: Double,
    val passed: Boolean,
    val failureMessage: String?,
    val failureType: String?,
    val failureContent: String?,
    val createdAt: Date,
)
