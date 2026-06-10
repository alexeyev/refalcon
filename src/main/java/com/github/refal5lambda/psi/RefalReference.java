package com.github.refal5lambda.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Resolves a function call ({@link RefalCall}) to its definition: first in the same file, then in
 * any other {@code .ref}/{@code .refi} file in the project (so {@code $EXTERN} calls resolve too).
 */
final class RefalReference extends PsiReferenceBase<PsiElement> {

    RefalReference(@NotNull PsiElement element) {
        // soft = true: calls to library/external functions just don't resolve, without being flagged.
        super(element, new TextRange(0, element.getTextLength()), true);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        String name = getElement().getText();
        if (name.isEmpty()) return null;

        // 1) Same file (fast path, and definitions here take precedence).
        RefalFunction local = findInFile(getElement().getContainingFile(), name);
        if (local != null) return local;

        // 2) Anywhere else in the project — via the cached project-wide symbol map (built once per
        //    PSI change and shared by all resolves and Go to Symbol), instead of rescanning and
        //    parsing every Refal file on each resolve.
        List<SmartPsiElementPointer<RefalFunction>> defs =
                RefalProjectSymbols.get(getElement().getProject()).get(name);
        if (defs != null) {
            for (SmartPsiElementPointer<RefalFunction> pointer : defs) {
                RefalFunction fn = pointer.getElement();
                if (fn != null) return fn;
            }
        }
        return null;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newName) {
        PsiElement call = getElement();                 // the RefalCall composite
        PsiElement oldLeaf = call.getFirstChild();       // the FUNCTION_CALL token inside it
        PsiElement newLeaf = RefalElementFactory.createCallLeaf(call.getProject(), newName);
        if (oldLeaf != null && newLeaf != null) {
            call.getNode().replaceChild(oldLeaf.getNode(), newLeaf.getNode());
        }
        return call;
    }

    @Nullable
    private static RefalFunction findInFile(@Nullable PsiFile file, String name) {
        if (!(file instanceof RefalFile)) return null;
        for (RefalFunction fn : PsiTreeUtil.findChildrenOfType(file, RefalFunction.class)) {
            if (name.equals(fn.getName())) return fn;
        }
        return null;
    }
}
