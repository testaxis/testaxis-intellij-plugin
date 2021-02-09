package io.testaxis.intellijplugin.toolwindow.builds.filters

import com.intellij.execution.util.StoringPropertyContainer
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.util.config.AbstractProperty
import com.intellij.util.config.BooleanProperty
import com.intellij.util.config.DumbAwareToggleInvertedBooleanProperty
import com.intellij.util.config.Storage
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.toolwindow.Icons

class PrFilter(val executeAfterFilterIsApplied: () -> Unit) : Filter {
    private val properties = object : StoringPropertyContainer(Storage.MapStorage()) {
        val hidePrBuilds = BooleanProperty("hidePrBuilds", true)

        override fun <T : Any?> onPropertyChanged(property: AbstractProperty<T>, value: T) {
            executeAfterFilterIsApplied()
        }
    }

    override val actionComponent = DefaultActionGroup().apply {
        addSeparator()
        add(
            DumbAwareToggleInvertedBooleanProperty(
                "Show PR Builds",
                "Show PR Builds",
                Icons.PullRequest,
                properties,
                properties.hidePrBuilds
            )
        )
    }

    override fun filter(build: Build) = if (properties.hidePrBuilds.get(properties)) build.pr.isNullOrEmpty() else true
}
