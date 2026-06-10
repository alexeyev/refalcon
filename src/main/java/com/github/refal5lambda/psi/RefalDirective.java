package com.github.refal5lambda.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

/** A {@code $DIRECTIVE ... ;} statement. */
public final class RefalDirective extends ASTWrapperPsiElement {
    public RefalDirective(@NotNull ASTNode node) {
        super(node);
    }
}
