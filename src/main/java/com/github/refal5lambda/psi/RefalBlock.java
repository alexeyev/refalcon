package com.github.refal5lambda.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

/** A balanced {@code { ... }} block (a function body). */
public final class RefalBlock extends ASTWrapperPsiElement {
    public RefalBlock(@NotNull ASTNode node) {
        super(node);
    }
}
