package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.openapi.vcs.changes.Change
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import io.testaxis.intellijplugin.vcs.CoveredFile
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer

internal class CoveredFileCellRenderer : BorderLayoutPanel(), ListCellRenderer<CoveredFile> {
    override fun getListCellRendererComponent(
        list: JList<out CoveredFile>,
        coveredFile: CoveredFile,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ): Component {
        font = list.font
        background = determineBackgroundColor(list, selected)
        foreground = determineForegroundColor(list, selected)

        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        removeAll()
        add(createTopRow(coveredFile))

        addToBottom(createBottomRow(coveredFile))

        return this
    }

    private fun createTopRow(coveredFile: CoveredFile) = BorderLayoutPanel().also { panel ->
        panel.background = this.background
        panel.foreground = this.foreground

        panel.add(
            JLabel(coveredFile.name()).also {
                it.foreground = this.foreground
                it.font = it.font.deriveFont(Font.BOLD)
            }
        )

        coveredFile.vcsChange?.type?.let { panel.addToRight(createChangeLabel(it)) }
    }

    private fun createBottomRow(coveredFile: CoveredFile) =
        JLabel(coveredFile.fileName).also {
            it.foreground = this.foreground?.darker()
        }

    private fun createChangeLabel(changeType: Change.Type) = BorderLayoutPanel().also { panel ->
        panel.background = changeType.color()
        panel.border = BorderFactory.createEmptyBorder(2, 4, 2, 4)

        panel.add(
            JLabel(changeType.label()).also {
                it.foreground = Color.white
            }
        )
    }

    private fun determineBackgroundColor(list: JList<out CoveredFile>, selected: Boolean) =
        if (UIUtil.isUnderWin10LookAndFeel()) {
            if (selected) list.selectionBackground else list.background
        } else {
            if (selected) list.selectionBackground else null
        }

    private fun determineForegroundColor(list: JList<out CoveredFile>, selected: Boolean) =
        if (!isEnabled) UIUtil.getLabelDisabledForeground() else {
            if (selected) list.selectionForeground else list.foreground
        }

    private fun Change.Type.color() = when (this) {
        Change.Type.MODIFICATION -> JBColor.BLUE
        Change.Type.NEW -> JBColor.GREEN
        Change.Type.DELETED -> JBColor.RED
        Change.Type.MOVED -> JBColor.GRAY
    }

    private fun Change.Type.label() = when (this) {
        Change.Type.MODIFICATION -> "Modified"
        Change.Type.NEW -> "New"
        Change.Type.DELETED -> "Deleted"
        Change.Type.MOVED -> "Moved"
    }
}
