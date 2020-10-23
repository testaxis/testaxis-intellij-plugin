package io.testaxis.intellijplugin.toolwindow.builds.tree

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.treeStructure.SimpleNode
import javax.swing.Icon

class RootNode(private val builds: List<FakeBuild>) : SimpleNode() {
    override fun getChildren() = builds.map { BuildNode(it) }.toTypedArray()

    override fun update(data: PresentationData) {
        super.update(data)
        data.presentableText = "<invisible root node>"
    }
}

class BuildNode(val build: FakeBuild) : SimpleNode() {
    override fun getChildren() = build.testCases.map { TestCaseNode(it) }.toTypedArray()

    override fun update(data: PresentationData) {
        super.update(data)

        data.presentableText = build.name
        data.tooltip = build.name

        data.setIcon(if (build.wasSuccessful()) AllIcons.General.InspectionsOK else AllIcons.General.Error)
    }
}

class TestCaseNode(val testCase: FakeTestCase) : SimpleNode() {
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
