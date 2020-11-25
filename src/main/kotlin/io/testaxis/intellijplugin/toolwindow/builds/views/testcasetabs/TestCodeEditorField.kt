package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.ide.highlighter.HighlighterFactory
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.ui.LanguageTextField

class TestCodeEditorField(project: Project) : LanguageTextField(null, project, "PLACEHOLDER") {
    init {
        isOneLineMode = false
        autoscrolls = true
    }

    override fun createEditor(): EditorEx = super.createEditor().apply {
        setVerticalScrollbarVisible(true)
        setHorizontalScrollbarVisible(true)
        colorsScheme = EditorColorsManager.getInstance().globalScheme
        setCaretEnabled(true)
        setCaretVisible(true)
        settings.isLineNumbersShown = true
        settings.isAdditionalPageAtBottom = true

        highlighter = HighlighterFactory.createHighlighter(project, fileType)
    }

    fun showTestMethod(method: PsiMethod?) {
        if (method == null) {
            document = EditorFactory.getInstance().createDocument("Test method could not be found.")
            return
        }

        setNewDocumentAndFileType(
            method.containingFile.fileType,
            PsiDocumentManager.getInstance(project).getDocument(method.containingFile)
        )

        setCaretPosition(method.textOffset)
        scrollToCaretPosition()
    }

    fun scrollToCaretPosition() =
        with(editor ?: throw IllegalStateException("Cannot scroll to caret when editor is not yet created.")) {
            scrollingModel.scrollVertically(offsetToPoint2D(caretModel.offset).y.toInt())
        }
}
