package com.github.refal5lambda;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Quick-fix for "Unresolved function": appends a stub definition
 * <pre>
 * Name {
 *   = ;
 * }
 * </pre>
 * at the end of the file and places the caret inside, ready to type the body. Implemented as a
 * plain document edit — the lenient parser rebuilds the PSI from text anyway, so text-level
 * insertion is the simplest correct approach here.
 */
final class RefalCreateFunctionFix implements IntentionAction {

    private final String name;

    RefalCreateFunctionFix(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getText() {
        return "Create function '" + name + "'";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Create Refal function";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file != null && file.getLanguage() == RefalLanguage.INSTANCE;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
        Document document = editor.getDocument();
        int end = document.getTextLength();
        CharSequence text = document.getCharsSequence();
        String separator = (end > 0 && text.charAt(end - 1) == '\n') ? "\n" : "\n\n";
        String head = separator + name + " {\n  = ";
        document.insertString(end, head + ";\n}\n");
        editor.getCaretModel().moveToOffset(end + head.length());
    }
}
