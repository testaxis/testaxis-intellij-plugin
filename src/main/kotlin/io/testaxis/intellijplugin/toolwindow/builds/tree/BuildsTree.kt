package io.testaxis.intellijplugin.toolwindow.builds.tree

import com.intellij.openapi.Disposable
import com.intellij.ui.SpeedSearchComparator
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.treeStructure.SimpleTreeStructure
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeSelectionModel

data class FakeTestCase(val name: String, val passed: Boolean = true)
data class FakeBuild(val name: String, val testCases: List<FakeTestCase>) {
    fun wasSuccessful() = testCases.all { it.passed }
}

class BuildsTree : Disposable {
    val buildSelectedListeners = mutableListOf<(FakeBuild) -> Unit>()
    val testCaseSelectedListeners = mutableListOf<(FakeTestCase) -> Unit>()

    private val tree = Tree().apply {
        emptyText.text = "There are no builds to show yet."

        isRootVisible = false
        TreeSpeedSearch(this).comparator = SpeedSearchComparator(false)
        TreeUtil.installActions(this)
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        isEditable = false

        rowHeight = 0

        addTreeSelectionListener {
            when (val simpleNode = (it.path.lastPathComponent as DefaultMutableTreeNode).userObject) {
                is BuildNode -> {
                    buildSelectedListeners.forEach { it(simpleNode.build) }
                }
                is TestCaseNode -> {
                    testCaseSelectedListeners.forEach { it(simpleNode.testCase) }
                }
            }
        }
    }

    fun render() = tree

    fun updateData(data: List<FakeBuild>) {
        tree.invalidate()

        tree.model = createTreeModel(data)
    }

    private fun createTreeModel(data: List<FakeBuild>): TreeModel {
        val root = RootNode(data)
        val treeModel = StructureTreeModel(SimpleTreeStructure.Impl(root), this)
        return AsyncTreeModel(treeModel, this)
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}
