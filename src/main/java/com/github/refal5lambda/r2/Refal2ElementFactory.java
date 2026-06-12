package com.github.refal5lambda.r2;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/** Creates Refal-2 PSI leaves from dummy files (used by rename). */
final class Refal2ElementFactory {
    private Refal2ElementFactory() {}

    static PsiElement createDefName(@NotNull Project project, @NotNull String name) {
        PsiFile file = dummy(project, name + " = x\n");
        Refal2Function fn = PsiTreeUtil.findChildOfType(file, Refal2Function.class);
        if (fn == null || fn.getNameIdentifier() == null) {
            throw new IllegalStateException("Invalid Refal-2 function name: " + name);
        }
        return fn.getNameIdentifier();
    }

    static PsiElement createCallName(@NotNull Project project, @NotNull String name) {
        PsiFile file = dummy(project, "x = <" + name + ">\n");
        Refal2Call call = PsiTreeUtil.findChildOfType(file, Refal2Call.class);
        if (call == null || call.getFirstChild() == null) {
            throw new IllegalStateException("Invalid Refal-2 call name: " + name);
        }
        return call.getFirstChild();
    }

    private static PsiFile dummy(Project project, String text) {
        return PsiFileFactory.getInstance(project)
                .createFileFromText("dummy.ref", Refal2Language.INSTANCE, text);
    }
}
