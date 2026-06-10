package com.github.refal5lambda.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A function call (the called name inside {@code <...>} / {@code [...]}); resolves to its definition. */
public final class RefalCall extends ASTWrapperPsiElement {
    public RefalCall(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        PsiReference[] refs = getReferences();
        return refs.length > 0 ? refs[0] : null;
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        if (getTextLength() == 0) return PsiReference.EMPTY_ARRAY;
        return new PsiReference[]{new RefalReference(this)};
    }
}
