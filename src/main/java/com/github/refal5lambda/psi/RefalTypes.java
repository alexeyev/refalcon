package com.github.refal5lambda.psi;

import com.github.refal5lambda.RefalElementType;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/** Composite (parser-produced) element types and PSI factory. */
public final class RefalTypes {
    private RefalTypes() {}

    public static final IElementType FUNCTION  = new RefalElementType("FUNCTION");
    public static final IElementType DIRECTIVE = new RefalElementType("DIRECTIVE");
    public static final IElementType BLOCK     = new RefalElementType("BLOCK");
    public static final IElementType NAME      = new RefalElementType("NAME");
    public static final IElementType CALL      = new RefalElementType("CALL");
    public static final IElementType SENTENCE  = new RefalElementType("SENTENCE");
    public static final IElementType PATTERN   = new RefalElementType("PATTERN");
    public static final IElementType RESULT    = new RefalElementType("RESULT");
    public static final IElementType EXPR      = new RefalElementType("EXPR");

    public static PsiElement createElement(ASTNode node) {
        IElementType type = node.getElementType();
        if (type == FUNCTION)  return new RefalFunction(node);
        if (type == DIRECTIVE) return new RefalDirective(node);
        if (type == BLOCK)     return new RefalBlock(node);
        if (type == CALL)      return new RefalCall(node);
        return new ASTWrapperPsiElement(node);
    }
}
