package com.github.refal5lambda.r2;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/** A Refal-2 function definition: identifier at column 0 plus its sentences. */
public final class Refal2Function extends ASTWrapperPsiElement implements PsiNameIdentifierOwner {

    public Refal2Function(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        ASTNode name = getNode().findChildByType(Refal2TokenTypes.NAME);
        return name == null ? null : name.getPsi().getFirstChild();
    }

    @Override
    public String getName() {
        PsiElement id = getNameIdentifier();
        return id == null ? null : id.getText();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        PsiElement id = getNameIdentifier();
        if (id != null) {
            id.replace(Refal2ElementFactory.createDefName(getProject(), name));
        }
        return this;
    }

    @Override
    public int getTextOffset() {
        PsiElement id = getNameIdentifier();
        return id != null ? id.getTextOffset() : super.getTextOffset();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new com.intellij.ide.projectView.PresentationData(
                getName(), getContainingFile().getName(), getIcon(0), null);
    }

    @Override
    public Icon getIcon(int flags) {
        return com.github.refal5lambda.RefalIcons.FILE;
    }
}
