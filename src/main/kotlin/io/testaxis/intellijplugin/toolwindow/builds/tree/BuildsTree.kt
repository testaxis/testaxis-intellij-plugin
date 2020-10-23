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
import javax.swing.tree.TreeSelectionModel

data class FakeTestCase(val name: String, val passed: Boolean = true)
data class FakeBuild(val name: String, val testCases: List<FakeTestCase>) {
    fun wasSuccessful() = testCases.all { it.passed }
}

val fakeData = listOf(
    FakeBuild("Build PR #2", listOf(
        FakeTestCase("It persists a user's username"),
        FakeTestCase("It persists a user's password"),
        FakeTestCase("It persists a user's address"),
        FakeTestCase("It persists a user's email"),
    )),
    FakeBuild("Build PR #3", listOf(
        FakeTestCase("It persists a user's username", passed = false),
        FakeTestCase("It persists a user's password", passed = false),
        FakeTestCase("It persists a user's address"),
        FakeTestCase("It persists a user's email"),
    )),
    FakeBuild("Build PR #4", listOf(
        FakeTestCase("It persists a user's username", passed = false),
        FakeTestCase("It persists a user's password"),
        FakeTestCase("It persists a user's address"),
        FakeTestCase("It persists a user's email"),
    )),
)

class BuildsTree : Disposable {
    val buildSelectedListeners = mutableListOf<(FakeBuild) -> Unit>()
    val testCaseSelectedListeners = mutableListOf<(FakeTestCase) -> Unit>()

    fun createTree(): Tree {
        val root = RootNode(fakeData)

        val treeModel = StructureTreeModel(SimpleTreeStructure.Impl(root), this)
        val asyncTreeModel = AsyncTreeModel(treeModel, this)

        val tree = Tree(asyncTreeModel)
        tree.emptyText.text = "There are no builds to show yet."

        tree.isRootVisible = false
        TreeSpeedSearch(tree).comparator = SpeedSearchComparator(false)
        TreeUtil.installActions(tree)
        tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        tree.isEditable = false

        tree.rowHeight = 0

        tree.addTreeSelectionListener {
            when (val simpleNode = (it.path.lastPathComponent as DefaultMutableTreeNode).userObject) {
                is BuildNode -> {
                    println("Selected a build: ${simpleNode.build}")
                    buildSelectedListeners.forEach { it(simpleNode.build) }
                }
                is TestCaseNode -> {
                    println("Selected a test: ${simpleNode.testCase}")
                    testCaseSelectedListeners.forEach { it(simpleNode.testCase) }
                }
            }
        }

        return tree
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}
