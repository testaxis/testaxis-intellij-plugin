package io.testaxis.intellijplugin.services

import com.intellij.openapi.project.Project
import io.testaxis.intellijplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
