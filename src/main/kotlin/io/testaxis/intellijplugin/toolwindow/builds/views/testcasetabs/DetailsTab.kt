package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.execution.testframework.ui.TestsConsoleBuilderImpl
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.JBColor
import com.intellij.ui.LightColors
import com.intellij.ui.components.Label
import com.intellij.ui.components.Link
import io.testaxis.intellijplugin.diffForHumans
import io.testaxis.intellijplugin.healthwarnings.investigateHealth
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.TestCaseExecution
import io.testaxis.intellijplugin.toolwindow.CirclePanel
import io.testaxis.intellijplugin.toolwindow.Icons
import io.testaxis.intellijplugin.toolwindow.borderLayoutPanel
import io.testaxis.intellijplugin.toolwindow.builds.views.BuildsUpdateHandler
import io.testaxis.intellijplugin.toolwindow.horizontal
import io.testaxis.intellijplugin.toolwindow.vertical
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder

class DetailsTab(val project: Project) : TestCaseTab, Disposable, BuildsUpdateHandler {
    override val tabName = "Execution Details"

    private lateinit var testCaseExecution: TestCaseExecution

    private val iconLabel = JLabel()
    private val nameLabel = Label("", bold = true)
    private val testSuiteNameLabel = Label("").apply { foreground = foreground.darker() }
    private val timeLabel = Label("").apply { icon = Icons.Clock }
    private val createdAtLabel = Label("").apply { icon = Icons.Time }

    private var buildHistory: List<Build> = emptyList()

    private val failureContentConsole =
        TestsConsoleBuilderImpl(
            project,
            GlobalSearchScope.allScope(project),
            true,
            true
        ).console

    private val testHealthPanel = vertical()

    private val panel = borderLayoutPanel {
        addToTop(
            borderLayoutPanel(10) {
                addToLeft(vertical(CirclePanel(iconLabel, background = JBColor.PanelBackground.brighter())))
                add(
                    vertical(
                        horizontal(
                            nameLabel,
                            Link("Open Test") { testCaseExecution.navigate() }
                        ),
                        horizontal(testSuiteNameLabel),
                        horizontal(testHealthPanel)
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

        testHealthPanel.removeAll()
        testCaseExecution.investigateHealth(project, buildHistory) {
            // Verify that the currently active test case has not changed
            if (this.testCaseExecution == testCaseExecution) {
                addTestHealthWarning(it)
            }
        }
    }

    override fun activate() {
        failureContentConsole.component.isVisible = false

        GlobalScope.launch {
            with(testCaseExecution.details(project)) {
                runInEdt {
                    if (failureContent != null) {
                        failureContentConsole.component.isVisible = true
                        failureContentConsole.clear()
                        failureContentConsole.print(failureContent, ConsoleViewContentType.ERROR_OUTPUT)
                    }
                }
            }
        }
    }

    private fun addTestHealthWarning(message: String) {
        val panel =
            borderLayoutPanel {
                add(
                    borderLayoutPanel {
                        add(Label(message).apply { icon = AllIcons.General.Warning })
                    }.apply {
                        border = EmptyBorder(5, 10, 5, 10)
                        background = LightColors.YELLOW
                    }
                )
            }.apply {
                border = EmptyBorder(10, 0, 0, 0)
            }

        testHealthPanel.add(panel)
    }

    override fun handleNewBuilds(buildHistory: List<Build>) {
        this.buildHistory = buildHistory
    }

    private fun TestCaseExecution.navigate() =
        getMethod(project)?.navigate(true)
            ?: Messages.showMessageDialog(
                project,
                "Please make sure that this test is present in your locally checked out code base.",
                "Test case could not be found",
                Messages.getErrorIcon()
            )

    override fun dispose() {
        failureContentConsole.dispose()
    }
}
