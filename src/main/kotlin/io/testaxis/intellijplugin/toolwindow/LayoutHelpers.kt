package io.testaxis.intellijplugin.toolwindow

import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.RenderingHints
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel

fun borderLayoutPanel(hgap: Int = 0, vgap: Int = 0, applyContent: BorderLayoutPanel.() -> Unit) =
    BorderLayoutPanel(hgap, vgap).apply(applyContent)

fun vertical(
    vararg components: JComponent,
    constraints: GridBagConstraints.() -> Unit = {},
    applyContent: JPanel.() -> Unit = {}
): JPanel =
    JPanel().apply {
        layout = GridBagLayout()

        val componentConstraints = GridBagConstraints().apply {
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            gridwidth = GridBagConstraints.REMAINDER

            anchor = GridBagConstraints.NORTH
            weighty = 1.0

            constraints()
        }

        components.forEach {
            add(it, componentConstraints)
        }

        applyContent()
    }

fun horizontal(vararg components: JComponent, hgap: Int = 5, applyContent: JPanel.() -> Unit = {}): JPanel =
    JPanel().apply {
        layout = FlowLayout(FlowLayout.LEFT, hgap, 0)

        components.forEach { add(it) }

        applyContent()
    }

class CirclePanel(vararg components: JComponent, background: Color? = null) : JPanel() {
    init {
        components.forEach { add(it) }
        this.background = background
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
    }

    override fun paintComponent(graphics: Graphics) {
        graphics as Graphics2D

        graphics.color = background

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.fillOval(0, 0, graphics.clipBounds.width - 1, graphics.clipBounds.height - 1)
        graphics.drawOval(0, 0, graphics.clipBounds.width - 1, graphics.clipBounds.height - 1)
    }
}
