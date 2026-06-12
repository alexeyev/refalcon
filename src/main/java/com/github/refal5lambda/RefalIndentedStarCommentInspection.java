package com.github.refal5lambda;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The classic Refal beginner trap, common to ALL dialects (Refal-2, Refal-5, Refal-5λ): a
 * {@code *} starts a comment <b>only at column 0</b>. An indented {@code * note} is code and the
 * compiler rejects it. This inspection flags such lines and offers to move them to column 0.
 *
 * <p>Lines whose {@code *} actually sits inside a comment or string (e.g. inside a Refal-5λ
 * block comment) are skipped.
 */
public final class RefalIndentedStarCommentInspection extends LocalInspectionTool {

    @Override
    public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file,
                                                    @NotNull InspectionManager manager,
                                                    boolean isOnTheFly) {
        String langId = file.getLanguage().getID();
        if (!"Refal5Lambda".equals(langId) && !"Refal2".equals(langId)) return null;

        String text = file.getText();
        List<ProblemDescriptor> problems = new ArrayList<>();
        int lineStart = 0;
        while (lineStart < text.length()) {
            int lineEnd = text.indexOf('\n', lineStart);
            if (lineEnd < 0) lineEnd = text.length();
            int i = lineStart;
            while (i < lineEnd && (text.charAt(i) == ' ' || text.charAt(i) == '\t')) i++;
            if (i > lineStart && i < lineEnd && text.charAt(i) == '*') {
                PsiElement leaf = file.findElementAt(i);
                if (leaf != null && !(leaf instanceof PsiComment) && !insideCommentOrString(leaf)) {
                    problems.add(manager.createProblemDescriptor(leaf,
                            "An asterisk starts a comment only at column 0 \u2014 indented, this line is code",
                            new MoveToColumnZeroFix(),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly));
                }
            }
            lineStart = lineEnd + 1;
        }
        return problems.isEmpty() ? null : problems.toArray(new ProblemDescriptor[0]);
    }

    private static boolean insideCommentOrString(PsiElement leaf) {
        String type = String.valueOf(leaf.getNode().getElementType());
        return type.contains("COMMENT") || type.contains("STRING") || type.contains("NATIVE");
    }

    private static final class MoveToColumnZeroFix implements LocalQuickFix {
        @Override
        public @NotNull String getFamilyName() {
            return "Move comment to column 0";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            if (element == null) return;
            PsiFile file = element.getContainingFile();
            Document document = PsiDocumentManager.getInstance(project).getDocument(file);
            if (document == null) return;
            int start = element.getTextRange().getStartOffset();
            int lineStart = document.getLineStartOffset(document.getLineNumber(start));
            if (start > lineStart) {
                document.deleteString(lineStart, start);
            }
        }
    }
}
