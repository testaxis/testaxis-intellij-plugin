package io.testaxis.intellijplugin.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod
import io.testaxis.intellijplugin.services.PsiService
import io.testaxis.intellijplugin.services.TestAxisApiService
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class TestCaseExecution(
    val id: Int,
    val testSuiteName: String,
    val name: String,
    val className: String,
    val time: Double,
    val passed: Boolean,
    val createdAt: Date,
) {
    suspend fun details() = service<TestAxisApiService>().getTestCaseExecutionDetails(this)

    fun getMethod(project: Project): PsiMethod? =
        project.service<PsiService>().findMethodByFullyQualifiedName(className, name)
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class TestCaseExecutionDetails(
    val failureMessage: String?,
    val failureType: String?,
    val failureContent: String?,
)
