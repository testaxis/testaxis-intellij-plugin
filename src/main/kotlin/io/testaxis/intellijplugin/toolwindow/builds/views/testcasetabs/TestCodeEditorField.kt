package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.ide.highlighter.HighlighterFactory
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.ColorKey
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.ui.LanguageTextField

private const val LINE_HIGHLIGHT_LAYER = 5950

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

    fun showFile(file: PsiFile?) {
        if (file == null) {
            document = EditorFactory.getInstance().createDocument("This file could not be found in the project.")
            return
        }

        setNewDocumentAndFileType(
            file.fileType,
            PsiDocumentManager.getInstance(project).getDocument(file)
        )

        setCaretPosition(0)
        scrollToCaretPosition()
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

    fun showText(text: String) {
        document = EditorFactory.getInstance().createDocument(text)
    }

    fun highlightLine(lineNumber: Int) = editor?.markupModel?.addLineHighlighter(
        lineNumber - 1,
        LINE_HIGHLIGHT_LAYER,
        createHighlightAttributes(EditorColors.ADDED_LINES_COLOR)
    )

    fun highlightElement(element: PsiElement) = editor?.markupModel?.addRangeHighlighter(
        element.textRange.startOffset,
        element.textRange.endOffset,
        LINE_HIGHLIGHT_LAYER,
        createHighlightAttributes(EditorColors.MODIFIED_LINES_COLOR),
        HighlighterTargetArea.LINES_IN_RANGE
    )

    private fun createHighlightAttributes(colorKey: ColorKey) = TextAttributes().apply {
        backgroundColor = editor?.colorsScheme?.getColor(colorKey)
        effectType = EffectType.ROUNDED_BOX
    }

    private fun scrollToCaretPosition() =
        with(editor ?: throw IllegalStateException("Cannot scroll to caret when editor is not yet created.")) {
            scrollingModel.scrollVertically(offsetToPoint2D(caretModel.offset).y.toInt())
        }
}
