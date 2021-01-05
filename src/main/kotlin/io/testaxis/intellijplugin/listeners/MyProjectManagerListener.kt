package io.testaxis.intellijplugin.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import io.testaxis.intellijplugin.notifications.BuildNotifier
import io.testaxis.intellijplugin.services.WebSocketService

internal class MyProjectManagerListener : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        project.service<WebSocketService>()
        BuildNotifier(project).listenForNewBuilds()
    }
}
