package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalFile;
import com.github.refal5lambda.psi.RefalFunction;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class RefalStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
    private final PsiElement element;

    public RefalStructureViewElement(@NotNull PsiElement element) {
        this.element = element;
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (element instanceof Navigatable) ((Navigatable) element).navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return element instanceof Navigatable && ((Navigatable) element).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return element instanceof Navigatable && ((Navigatable) element).canNavigateToSource();
    }

    @NotNull
    @Override
    public String getAlphaSortKey() {
        if (element instanceof RefalFunction) {
            String n = ((RefalFunction) element).getName();
            if (n != null) return n;
        }
        return element instanceof RefalFile ? ((RefalFile) element).getName() : "";
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        if (element instanceof RefalFunction) {
            ItemPresentation p = ((RefalFunction) element).getPresentation();
            if (p != null) return p;
        }
        if (element instanceof RefalFile) {
            return new PresentationData(((RefalFile) element).getName(), null, RefalIcons.FILE, null);
        }
        return new PresentationData(getAlphaSortKey(), null, null, null);
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        if (element instanceof RefalFile) {
            List<TreeElement> children = new ArrayList<>();
            for (RefalFunction fn : PsiTreeUtil.getChildrenOfTypeAsList(element, RefalFunction.class)) {
                children.add(new RefalStructureViewElement(fn));
            }
            return children.toArray(new TreeElement[0]);
        }
        return StructureViewTreeElement.EMPTY_ARRAY;
    }
}
