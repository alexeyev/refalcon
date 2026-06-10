package com.github.refal5lambda.psi;

import com.github.refal5lambda.RefalTokenTypes;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A top-level function definition: a name followed by a {@code { ... }} block. */
public final class RefalFunction extends ASTWrapperPsiElement implements PsiNameIdentifierOwner {
    public RefalFunction(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        ASTNode nameNode = getNode().findChildByType(RefalTypes.NAME);
        if (nameNode == null) return null;
        ASTNode id = nameNode.findChildByType(RefalTokenTypes.FUNCTION_DEFINITION);
        return id != null ? id.getPsi() : null;
    }

    @Nullable
    @Override
    public String getName() {
        PsiElement id = getNameIdentifier();
        return id != null ? id.getText() : null;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        PsiElement id = getNameIdentifier();
        if (id != null && id.getParent() != null) {
            PsiElement newId = RefalElementFactory.createNameLeaf(getProject(), name);
            if (newId != null) {
                id.getParent().getNode().replaceChild(id.getNode(), newId.getNode());
            }
        }
        return this;
    }

    @Override
    public int getTextOffset() {
        PsiElement id = getNameIdentifier();
        return id != null ? id.getTextOffset() : super.getTextOffset();
    }

    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        String n = getName();
        return new PresentationData(n != null ? n : "function", null, AllIcons.Nodes.Function, null);
    }
}
