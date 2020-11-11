package io.testaxis.intellijplugin.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import io.testaxis.intellijplugin.notifications.BuildNotifier

internal class MyProjectManagerListener : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        BuildNotifier(project).listenForNewBuilds()
    }
}
