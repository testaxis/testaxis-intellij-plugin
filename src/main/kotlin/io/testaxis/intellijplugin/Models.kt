package io.testaxis.intellijplugin

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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
data class TestCaseExecution(val name: String, val passed: Boolean = true)
