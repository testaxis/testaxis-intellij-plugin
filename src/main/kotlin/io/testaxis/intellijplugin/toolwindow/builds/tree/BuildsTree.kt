package io.testaxis.intellijplugin.toolwindow.builds.tree

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.SpeedSearchComparator
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.treeStructure.SimpleTreeStructure
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

class BuildsTree(val project: Project) : Disposable {
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

    fun selectAndExpand(build: Build) = (tree.model as AsyncTreeModel).let { treeModel ->
        treeModel.onValidThread {
            if (treeModel.root == null) {
                return@onValidThread
            }

            (treeModel.root as DefaultMutableTreeNode).children().toList().forEach {
                val node = it as DefaultMutableTreeNode

                val buildNode = node.userObject as BuildNode

                if (buildNode.build == build) {
                    tree.selectionPath = TreePath(node.path)
                    tree.expandPath(TreePath(node.path))
                }
            }
        }
    }

    fun selectedTestCase(): TestCaseExecution? =
        when (val simpleNode = (tree.selectionPath?.lastPathComponent as DefaultMutableTreeNode).userObject) {
            is TestCaseNode -> simpleNode.testCase
            else -> null
        }

    private fun createTreeModel(data: List<Build>): TreeModel {
        val root = RootNode(project, data)
        val treeModel = StructureTreeModel(SimpleTreeStructure.Impl(root), this)
        return AsyncTreeModel(treeModel, this)
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}
