package io.testaxis.intellijplugin.services

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

class PsiService(private val project: Project) {
    val scope = GlobalSearchScope.projectScope(project)

    /**
     * Finds a method using a fully qualified class name and a method name.
     *
     * @param className The fully qualified class name.
     * @param methodName The full name of the method. May include parentheses.
     */
    fun findMethodByFullyQualifiedName(className: String, methodName: String): PsiMethod? =
        JavaPsiFacade.getInstance(project)
            .findClass(className, scope)
            ?.findMethodsByName(methodName.substringBeforeLast('('), false)
            ?.also {
                if (it.size > 1) {
                    println("Found more than one test with the same name, picking the first one.")
                }
            }
            ?.firstOrNull()

    /**
     * Finds the (first) PSI file matching the given relative path.
     *
     * @param relativePath The relative path, e.g. "com/example/Counter.java".
     * @return the PSI file corresponding to the relative path.
     */
    fun findFileByRelativePath(relativePath: String): PsiFile? {
        return findVirtualFilesByRelativePath(relativePath)
            .mapNotNull { PsiManager.getInstance(project).findFile(it) }
            .firstOrNull()
    }

    private fun findVirtualFilesByRelativePath(fileRelativePath: String): List<VirtualFile> {
        val relativePath = if (fileRelativePath.startsWith('/')) fileRelativePath else "/$fileRelativePath"
        val fileTypes = setOf(FileTypeManager.getInstance().getFileTypeByFileName(relativePath))
        val fileList = mutableListOf<VirtualFile>()

        FileBasedIndex.getInstance().processFilesContainingAllKeys(FileTypeIndex.NAME, fileTypes, scope, null) {
            if (it.path.endsWith(relativePath)) {
                fileList.add(it)
            }
            true
        }

        return fileList
    }
}
