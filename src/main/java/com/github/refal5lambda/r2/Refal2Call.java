package com.github.refal5lambda.r2;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

/** A function-position word inside {@code <…>} or {@code k/…/} — carries the reference. */
public final class Refal2Call extends ASTWrapperPsiElement {

    public Refal2Call(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return new Refal2Reference(this);
    }
}
