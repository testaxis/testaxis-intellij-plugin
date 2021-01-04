package io.testaxis.intellijplugin.toolwindow.builds.filters

import com.intellij.openapi.actionSystem.AnAction
import io.testaxis.intellijplugin.models.Build

interface Filter {
    val actionComponent: AnAction
    fun filter(build: Build): Boolean
}
