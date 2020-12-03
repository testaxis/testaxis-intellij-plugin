package io.testaxis.intellijplugin.toolwindow.builds.views

import io.testaxis.intellijplugin.models.Build

interface BuildsUpdateHandler {
    fun handleNewBuilds(buildHistory: List<Build>)
}
