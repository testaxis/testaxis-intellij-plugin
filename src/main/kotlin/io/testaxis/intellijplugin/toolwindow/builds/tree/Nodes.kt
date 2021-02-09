package io.testaxis.intellijplugin.toolwindow.builds.tree

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.treeStructure.SimpleNode
import io.testaxis.intellijplugin.diffForHumans
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import kotlinx.coroutines.runBlocking
import javax.swing.Icon

interface SecondaryInformationHolder {
    fun getSecondaryInformation(): String
}

class RootNode(project: Project?, private val builds: List<Build>) : SimpleNode(project) {
    override fun getChildren() =
        builds.sortedByDescending { it.createdAt }.map { BuildNode(project, it) }.toTypedArray()

    override fun update(data: PresentationData) {
        super.update(data)
        data.presentableText = "<invisible root node>"
    }
}

class BuildNode(project: Project?, val build: Build) : SimpleNode(project), SecondaryInformationHolder {
    override fun getChildren() = listOf(
        GenericChildrenNode("Test Results") {
            runBlocking {
                build.retrieveTestCaseExecutions(project ?: error("Project not present."))
                    .groupBy { it.testSuiteName }
                    .map { entry -> TestGroupNode(entry.key, entry.value) }
                    .sortedBy { it.testCases.all(TestCaseExecution::passed) }
            }
        }
    ).toTypedArray()

    override fun update(data: PresentationData) {
        super.update(data)

        data.tooltip = build.labelMaker().withCreatedAt().createString()
        data.clearText()
        build.labelMaker().withCommitMessage().createItems().forEach { data.addText(it.text, it.attributes) }

        data.setIcon(build.status.icon)
    }

    override fun getSecondaryInformation() = build.createdAt.diffForHumans()
}

class TestGroupNode(
    val groupName: String,
    val testCases: List<TestCaseExecution>
) : SimpleNode(), SecondaryInformationHolder {
    override fun getChildren() = testCases.sortedBy { it.passed }.map { TestCaseNode(it) }.toTypedArray()

    override fun update(data: PresentationData) {
        super.update(data)

        data.presentableText = groupName
        data.tooltip = groupName

        data.setIcon(if (testCases.all { it.passed }) AllIcons.General.InspectionsOK else AllIcons.General.Error)
    }

    override fun getSecondaryInformation() =
        StringUtil.formatDuration((testCases.sumByDouble { it.time } * 1000).toLong())
}

class TestCaseNode(val testCase: TestCaseExecution) : SimpleNode(), SecondaryInformationHolder {
    override fun getChildren(): Array<SimpleNode> = NO_CHILDREN

    override fun update(data: PresentationData) {
        super.update(data)

        data.presentableText = testCase.name
        data.tooltip = testCase.name

        data.setIcon(if (testCase.passed) AllIcons.General.InspectionsOK else AllIcons.General.Error)
    }

    override fun getSecondaryInformation() = StringUtil.formatDuration((testCase.time * 1000).toLong())
}

class TextNode(private val label: String, private val nodeIcon: Icon? = null) : SimpleNode() {
    override fun getChildren(): Array<SimpleNode> = NO_CHILDREN

    override fun update(data: PresentationData) {
        super.update(data)

        data.presentableText = label
        data.tooltip = label

        nodeIcon?.let { data.setIcon(it) }
    }
}

class GenericChildrenNode(
    private val label: String,
    private val retrieveChildren: () -> List<SimpleNode>
) : SimpleNode() {
    override fun getChildren() = retrieveChildren().toTypedArray()

    override fun update(data: PresentationData) {
        super.update(data)

        data.presentableText = label
        data.tooltip = label
    }
}
