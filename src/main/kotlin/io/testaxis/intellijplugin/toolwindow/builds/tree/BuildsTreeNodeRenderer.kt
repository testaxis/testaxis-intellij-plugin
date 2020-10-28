package io.testaxis.intellijplugin.toolwindow.builds.tree

import com.intellij.ide.ui.UISettings
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.ui.RelativeFont
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ui.UIUtil
import java.awt.Color
import java.awt.Graphics
import java.awt.Shape
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

/**
 * Based on: [com.intellij.build.BuildTreeConsoleView.MyNodeRenderer].
 */
class BuildsTreeNodeRenderer : NodeRenderer() {
    private data class SecondaryText(val text: String, val color: Color, val width: Int, val offset: Int)

    private var secondaryText: SecondaryText? = null

    override fun customizeCellRenderer(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ) {
        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus)

        secondaryText = null

        val userObject = (value as DefaultMutableTreeNode).userObject

        if (userObject is SecondaryInformationHolder) {
            val text = userObject.getSecondaryInformation()
            val metrics = getFontMetrics(RelativeFont.SMALL.derive(font))
            val color =
                if (selected) UIUtil.getTreeSelectionForeground(hasFocus)
                else SimpleTextAttributes.GRAYED_ATTRIBUTES.fgColor

            secondaryText = SecondaryText(text, color, metrics.stringWidth(text), metrics.height / 2)
        }
    }

    override fun paintComponent(g: Graphics) {
        UISettings.setupAntialiasing(g)

        var clip: Shape? = null
        var width = width
        val height = height

        if (isOpaque) {
            // paint background for expanded row
            g.color = background
            g.fillRect(0, 0, width, height)
        }

        if (secondaryText != null) {
            width -= secondaryText!!.width + secondaryText!!.offset
            if (width > 0 && height > 0) {
                g.apply {
                    color = secondaryText!!.color
                    font = RelativeFont.SMALL.derive(font)
                    drawString(
                        secondaryText!!.text,
                        width + secondaryText!!.offset / 2,
                        getTextBaseLine(g.fontMetrics, height)
                    )
                    clip = g.clip
                    g.clipRect(0, 0, width, height)
                }
            }
        }

        super.paintComponent(g)

        // restore clip area if needed
        if (clip != null) g.clip = clip
    }
}
