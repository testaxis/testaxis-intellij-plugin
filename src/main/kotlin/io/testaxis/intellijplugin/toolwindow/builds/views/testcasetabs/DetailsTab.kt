package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.execution.testframework.ui.TestsConsoleBuilderImpl
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.JBColor
import com.intellij.ui.components.Label
import com.intellij.ui.components.Link
import com.intellij.util.ui.components.BorderLayoutPanel
import io.testaxis.intellijplugin.diffForHumans
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.toolwindow.Icons
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.RenderingHints
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder

class DetailsTab(val project: Project) : TestCaseTab {
    override val tabName = "Execution Details"

    private lateinit var testCaseExecution: TestCaseExecution

    private val iconLabel = JLabel()
    private val nameLabel = Label("", bold = true)
    private val testSuiteNameLabel = Label("").apply { foreground = foreground.darker() }
    private val timeLabel = Label("").apply { icon = Icons.Clock }
    private val createdAtLabel = Label("").apply { icon = Icons.Time }

    private fun borderLayoutPanel(hgap: Int = 0, vgap: Int = 0, applyContent: BorderLayoutPanel.() -> Unit) =
        BorderLayoutPanel(hgap, vgap).apply(applyContent)

    private fun vertical(
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

    private fun horizontal(vararg components: JComponent, applyContent: JPanel.() -> Unit = {}): JPanel =
        JPanel().apply {
            layout = FlowLayout(FlowLayout.LEFT, 5, 0)

            components.forEach { add(it) }

            applyContent()
        }

    private fun JButton.withAction(action: () -> Unit) = this.also { it.addActionListener { action() } }

    class CirclePanel(vararg components: JComponent, background: Color? = null) : JPanel() {
        init {
            components.forEach { add(it) }
            this.background = background
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        }

        override fun paintComponent(graphics: Graphics) {
            graphics as Graphics2D

            graphics.color = background

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.fillOval(0, 0, graphics.clipBounds.width - 1, graphics.clipBounds.height - 1)
            graphics.drawOval(0, 0, graphics.clipBounds.width - 1, graphics.clipBounds.height - 1)
        }
    }

    private val failureContentConsole =
        TestsConsoleBuilderImpl(
            project,
            GlobalSearchScope.allScope(project),
            true,
            true
        ).console

    private val panel = borderLayoutPanel {
        addToTop(
            borderLayoutPanel(10) {
                addToLeft(CirclePanel(iconLabel, background = JBColor.PanelBackground.brighter()))
                add(
                    vertical(
                        horizontal(
                            nameLabel,
                            Link("Open Test") { testCaseExecution.getMethod(project)?.navigate(true) }
                        ),
                        horizontal(testSuiteNameLabel)
                    )
                )
                addToRight(vertical(timeLabel.apply { horizontalAlignment = SwingConstants.RIGHT }, createdAtLabel))
            }
        )
        add(failureContentConsole.component.apply { border = EmptyBorder(20, 0, 0, 0) })
    }

    override fun getComponent(): JComponent = panel.apply {
        border = EmptyBorder(20, 20, 20, 20)
    }

    override fun setTestCaseExecution(testCaseExecution: TestCaseExecution) {
        this.testCaseExecution = testCaseExecution

        with(testCaseExecution) {
            iconLabel.icon = if (passed) AllIcons.General.InspectionsOK else AllIcons.General.Error
            nameLabel.text = name
            testSuiteNameLabel.text = testSuiteName
            timeLabel.text = StringUtil.formatDuration((time * 1000).toLong())
            createdAtLabel.text = createdAt.diffForHumans()
            createdAtLabel.toolTipText = createdAt.toString()
        }
    }

    override fun activate() {
        GlobalScope.launch {
            with(testCaseExecution.details()) {
                runInEdt {
                    if (failureContent == null) {
                        failureContentConsole.component.isVisible = false
                    } else {
                        failureContentConsole.component.isVisible = true
                        failureContentConsole.clear()
                        failureContentConsole.print(failureContent, ConsoleViewContentType.ERROR_OUTPUT)
                    }
                }
            }
        }
    }
}
