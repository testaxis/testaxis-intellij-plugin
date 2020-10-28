package io.testaxis.intellijplugin.toolwindow.builds.tree

import com.intellij.openapi.Disposable
import com.intellij.ui.SpeedSearchComparator
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.treeStructure.SimpleTreeStructure
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import io.testaxis.intellijplugin.Build
import io.testaxis.intellijplugin.TestCaseExecution
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeSelectionModel

class BuildsTree : Disposable {
    val buildSelectedListeners = mutableListOf<(Build) -> Unit>()
    val testCaseSelectedListeners = mutableListOf<(TestCaseExecution) -> Unit>()

    private val tree = Tree().apply {
        emptyText.text = "There are no builds to show yet."

        isRootVisible = false
        TreeSpeedSearch(this).comparator = SpeedSearchComparator(false)
        TreeUtil.installActions(this)
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        isEditable = false

        cellRenderer = BuildsTreeNodeRenderer()

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

    fun updateData(data: List<Build>) {
        tree.invalidate()

        tree.model = createTreeModel(data)
    }

    private fun createTreeModel(data: List<Build>): TreeModel {
        val root = RootNode(data)
        val treeModel = StructureTreeModel(SimpleTreeStructure.Impl(root), this)
        return AsyncTreeModel(treeModel, this)
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}
