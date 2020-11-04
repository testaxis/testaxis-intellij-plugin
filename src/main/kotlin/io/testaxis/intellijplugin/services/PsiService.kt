package io.testaxis.intellijplugin.services

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope

class PsiService(private val project: Project) {
    /**
     * Finds a method using a fully qualified class name and a method name.
     *
     * @param className The fully qualified class name.
     * @param methodName The full name of the method. May include parentheses.
     */
    fun findMethodByFullyQualifiedName(className: String, methodName: String): PsiMethod? =
        JavaPsiFacade.getInstance(project)
            .findClass(className, GlobalSearchScope.everythingScope(project))
            ?.findMethodsByName(methodName.substringBeforeLast('('), false)
            ?.also {
                if (it.size > 1) {
                    println("Found more than one test with the same name, picking the first one.")
                }
            }
            ?.firstOrNull()
}
