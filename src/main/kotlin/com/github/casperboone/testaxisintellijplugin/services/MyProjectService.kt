package com.github.casperboone.testaxisintellijplugin.services

import com.intellij.openapi.project.Project
import com.github.casperboone.testaxisintellijplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
