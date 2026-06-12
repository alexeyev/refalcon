package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalTypes;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;

/**
 * Quick-fix for "Unresolved function": declares the name with a {@code $EXTERN} directive — the
 * Refal way to call a function defined in another compilation unit.
 *
 * <p>The directive is inserted after the last <i>declaration-style</i> directive at the top of the
 * file (one that ends with {@code ;}, like another {@code $EXTERN}). Directives such as
 * {@code $ENTRY} that prefix the following function are deliberately not used as anchors —
 * inserting between {@code $ENTRY} and its function would break the program.
 */
final class RefalAddExternFix implements IntentionAction {

    private final String name;

    RefalAddExternFix(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getText() {
        return "Add '$EXTERN " + name + ";'";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Declare Refal function";
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
        int anchor = anchorOffset(file);
        if (anchor > 0) {
            document.insertString(anchor, "\n$EXTERN " + name + ";");
        } else {
            document.insertString(0, "$EXTERN " + name + ";\n");
        }
    }

    /** End offset of the last top-level directive that ends with ';', or 0 if there is none. */
    private static int anchorOffset(PsiFile file) {
        int anchor = 0;
        for (PsiElement child = file.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (PsiUtilCore.getElementType(child) == RefalTypes.FUNCTION) break;
            if (PsiUtilCore.getElementType(child) == RefalTypes.DIRECTIVE
                    && child.getText().trim().endsWith(";")) {
                anchor = child.getTextRange().getEndOffset();
            }
        }
        return anchor;
    }
}
