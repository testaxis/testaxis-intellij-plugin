package io.testaxis.intellijplugin.toolwindow.builds.filters

import com.intellij.execution.util.StoringPropertyContainer
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.util.config.AbstractProperty
import com.intellij.util.config.BooleanProperty
import com.intellij.util.config.DumbAwareToggleInvertedBooleanProperty
import com.intellij.util.config.Storage
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.models.BuildStatus

class StatusFilter(val executeAfterFilterIsApplied: () -> Unit) : Filter {
    private val properties = object : StoringPropertyContainer(Storage.MapStorage()) {
        val hidePassedBuilds = BooleanProperty("hidePassedBuilds", false)
        val hideFailedTestsBuilds = BooleanProperty("hideFailedTestsBuilds", false)
        val hideFailedBuilds = BooleanProperty("hideFailedBuilds", false)
        val hideUnknownBuilds = BooleanProperty("hideUnknownBuilds", false)

        override fun <T : Any?> onPropertyChanged(property: AbstractProperty<T>, value: T) {
            executeAfterFilterIsApplied()
        }
    }

    override val actionComponent = DefaultActionGroup().apply {
        addSeparator()
        add(
            DumbAwareToggleInvertedBooleanProperty(
                "Show Passed",
                "Show Passed Builds",
                BuildStatus.SUCCESS.icon,
                properties,
                properties.hidePassedBuilds
            )
        )
        add(
            DumbAwareToggleInvertedBooleanProperty(
                "Show Failed Tests Builds",
                "Show Failed Tests Builds",
                BuildStatus.TESTS_FAILED.icon,
                properties,
                properties.hideFailedTestsBuilds
            )
        )
        add(
            DumbAwareToggleInvertedBooleanProperty(
                "Show Failed",
                "Show Failed Builds",
                BuildStatus.BUILD_FAILED.icon,
                properties,
                properties.hideFailedBuilds
            )
        )
        add(
            DumbAwareToggleInvertedBooleanProperty(
                "Show Unknown",
                "Show Unknown Builds",
                BuildStatus.UNKNOWN.icon,
                properties,
                properties.hideUnknownBuilds
            )
        )
    }

    override fun filter(build: Build) = when (build.status) {
        BuildStatus.SUCCESS -> !properties.hidePassedBuilds.get(properties)
        BuildStatus.BUILD_FAILED -> !properties.hideFailedBuilds.get(properties)
        BuildStatus.TESTS_FAILED -> !properties.hideFailedTestsBuilds.get(properties)
        BuildStatus.UNKNOWN -> !properties.hideUnknownBuilds.get(properties)
    }
}
