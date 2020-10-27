package io.testaxis.intellijplugin.toolwindow.builds.tree

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.treeStructure.SimpleNode
import io.testaxis.intellijplugin.Build
import io.testaxis.intellijplugin.TestCaseExecution
import kotlinx.coroutines.runBlocking
import javax.swing.Icon

class RootNode(private val builds: List<Build>) : SimpleNode() {
    override fun getChildren() = builds.map { BuildNode(it) }.toTypedArray()

    override fun update(data: PresentationData) {
        super.update(data)
        data.presentableText = "<invisible root node>"
    }
}

class BuildNode(val build: Build) : SimpleNode() {
    override fun getChildren() = listOf(
        GenericChildrenNode("Tests") {
            runBlocking {
                build.retrieveTestCaseExecutions().map { TestCaseNode(it) }
            }
        }
    ).toTypedArray()

    override fun update(data: PresentationData) {
        super.update(data)

        data.presentableText = build.label()
        data.tooltip = build.label()

        @Suppress("ForbiddenComment")
        // TODO: Visualize result: if (build.wasSuccessful()) AllIcons.General.InspectionsOK else AllIcons.General.Error
        data.setIcon(AllIcons.General.InspectionsOK)
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
