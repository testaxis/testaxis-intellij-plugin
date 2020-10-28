package io.testaxis.intellijplugin.toolwindow.builds.tree

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.treeStructure.SimpleNode
import io.testaxis.intellijplugin.Build
import io.testaxis.intellijplugin.TestCaseExecution
import kotlinx.coroutines.runBlocking
import javax.swing.Icon

class RootNode(private val builds: List<Build>) : SimpleNode() {
    override fun getChildren() = builds.sortedByDescending { it.id }.map { BuildNode(it) }.toTypedArray()

    override fun update(data: PresentationData) {
        super.update(data)
        data.presentableText = "<invisible root node>"
    }
}

class BuildNode(val build: Build) : SimpleNode() {
    override fun getChildren() = listOf(
        GenericChildrenNode("Test Results") {
            runBlocking {
                build.retrieveTestCaseExecutions()
                    .groupBy { it.testSuiteName }
                    .map { entry -> TestGroupNode(entry.key, entry.value) }
                    .sortedBy { it.testCases.all(TestCaseExecution::passed) }
            }
        }
    ).toTypedArray()

    override fun update(data: PresentationData) {
        super.update(data)

        data.presentableText = build.label()
        data.tooltip = build.label()

        data.setIcon(icon())
    }

    private fun icon() = when (build.status) {
        "success" -> AllIcons.General.InspectionsOK
        "tests_failed" -> AllIcons.General.Error
        "build_failed" -> AllIcons.General.Warning
        else -> AllIcons.RunConfigurations.TestUnknown
    }
}

class TestGroupNode(val groupName: String, val testCases: List<TestCaseExecution>) : SimpleNode() {
    override fun getChildren() = testCases.sortedBy { it.passed }.map { TestCaseNode(it) }.toTypedArray()

    override fun update(data: PresentationData) {
        super.update(data)

        data.presentableText = groupName
        data.tooltip = groupName

        data.setIcon(if (testCases.all { it.passed }) AllIcons.General.InspectionsOK else AllIcons.General.Error)
    }
}

class TestCaseNode(val testCase: TestCaseExecution) : SimpleNode() {
    override fun getChildren(): Array<SimpleNode> = NO_CHILDREN

    override fun update(data: PresentationData) {
        super.update(data)

        data.presentableText = testCase.name
        data.tooltip = testCase.name

        data.setIcon(if (testCase.passed) AllIcons.General.InspectionsOK else AllIcons.General.Error)
    }
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
