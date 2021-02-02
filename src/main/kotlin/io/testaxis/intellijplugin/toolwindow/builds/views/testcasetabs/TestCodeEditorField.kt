package io.testaxis.intellijplugin.toolwindow.builds.views.testcasetabs

import com.intellij.ide.highlighter.HighlighterFactory
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.LogicalPosition
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
import com.intellij.ui.JBColor
import com.intellij.ui.LanguageTextField
import java.awt.Color

private const val LINE_HIGHLIGHT_LAYER = 5950
private const val COVERED_LINE_HIGHLIGHT_LAYER = 5951
private const val CHANGED_LINE_HIGHLIGHT_LAYER = 5952
private const val COVERED_AND_CHANGED_LINE_HIGHLIGHT_LAYER = 5953
private const val FRAGMENT_HIGHLIGHT_LAYER = 5960

@Suppress("TooManyFunctions")
class TestCodeEditorField(project: Project) : LanguageTextField(null, project, "PLACEHOLDER") {
    companion object {
        val COVERED_LINE_COLOR = JBColor.YELLOW.darker()
        val CHANGED_LINE_COLOR = JBColor.GREEN.darker().darker()
        val COVERED_AND_CHANGED_LINE_COLOR = JBColor.MAGENTA.darker()
    }

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

    fun highlightCoveredLine(lineNumber: Int) =
        editor?.markupModel?.addLineHighlighter(
            lineNumber - 1,
            COVERED_LINE_HIGHLIGHT_LAYER,
            createHighlightAttributes(COVERED_LINE_COLOR)
        )

    fun highlightChangedLine(lineNumber: Int) =
        editor?.markupModel?.addLineHighlighter(
            lineNumber - 1,
            CHANGED_LINE_HIGHLIGHT_LAYER,
            createHighlightAttributes(CHANGED_LINE_COLOR)
        )

    fun highlightCoveredAndChangedLine(lineNumber: Int) =
        editor?.markupModel?.addLineHighlighter(
            lineNumber - 1,
            COVERED_AND_CHANGED_LINE_HIGHLIGHT_LAYER,
            createHighlightAttributes(COVERED_AND_CHANGED_LINE_COLOR)
        )

    fun highlightElement(element: PsiElement?) = editor?.markupModel?.addRangeHighlighter(
        element?.textRange?.startOffset ?: 0,
        element?.textRange?.endOffset ?: 0,
        LINE_HIGHLIGHT_LAYER,
        createHighlightAttributes(editor?.colorsScheme?.getColor(EditorColors.MODIFIED_LINES_COLOR)),
        HighlighterTargetArea.LINES_IN_RANGE
    )

    private fun createHighlightAttributes(color: Color?) = TextAttributes().apply {
        backgroundColor = color
        effectType = EffectType.ROUNDED_BOX
    }

    fun moveCaretToLine(lineNumber: Int) =
        with(editor ?: throw IllegalStateException("Cannot set caret position when editor is not yet created.")) {
            caretModel.moveToLogicalPosition(LogicalPosition(lineNumber, 0))
        }

    fun scrollToCaretPosition() =
        with(editor ?: throw IllegalStateException("Cannot scroll to caret when editor is not yet created.")) {
            scrollingModel.scrollVertically(offsetToPoint2D(caretModel.offset).y.toInt())
        }
}
