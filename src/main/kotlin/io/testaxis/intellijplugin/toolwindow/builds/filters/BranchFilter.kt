package io.testaxis.intellijplugin.toolwindow.builds.filters

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.services.GitService
import javax.swing.JComponent

class BranchFilter(val project: Project, val executeAfterFilterIsApplied: () -> Unit) : Filter {
    private var branch: String? = null

    private val branchFilterActions = DefaultActionGroup()

    override val actionComponent = object : ComboBoxAction() {
        override fun createPopupActionGroup(button: JComponent?) = branchFilterActions

        override fun update(e: AnActionEvent) {
            e.presentation.text = "Branch: ${branch ?: "All"}"
        }
    }

    override fun filter(build: Build) = branch == null || build.branch == branch

    fun updateBranches(branches: List<String>) {
        branchFilterActions.removeAll()

        branchFilterActions.add(branchFilterAction("All", null))
        project.service<GitService>().currentBranch()?.let {
            branchFilterActions.add(branchFilterAction("Current: $it", it))
        }
        branchFilterActions.addSeparator()
        branches.forEach { branchFilterActions.add(branchFilterAction(it, it)) }
    }

    private fun branchFilterAction(text: String, branchName: String?) = object : DumbAwareAction(text) {
        override fun actionPerformed(e: AnActionEvent) {
            branch = branchName
            executeAfterFilterIsApplied()
        }
    }
}
