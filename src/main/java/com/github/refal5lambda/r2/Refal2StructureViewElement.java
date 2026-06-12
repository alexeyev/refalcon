package com.github.refal5lambda.r2;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class Refal2StructureViewElement implements StructureViewTreeElement, SortableTreeElement {

    private final NavigatablePsiElement element;

    Refal2StructureViewElement(@NotNull NavigatablePsiElement element) {
        this.element = element;
    }

    @Override public Object getValue() { return element; }
    @Override public void navigate(boolean requestFocus) { element.navigate(requestFocus); }
    @Override public boolean canNavigate() { return element.canNavigate(); }
    @Override public boolean canNavigateToSource() { return element.canNavigateToSource(); }

    @NotNull @Override
    public String getAlphaSortKey() {
        String name = element.getName();
        return name == null ? "" : name;
    }

    @NotNull @Override
    public ItemPresentation getPresentation() {
        ItemPresentation presentation = element.getPresentation();
        return presentation != null ? presentation : new PresentationData(element.getName(), null, null, null);
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
        if (element instanceof Refal2File) {
            Collection<Refal2Function> functions = PsiTreeUtil.findChildrenOfType(element, Refal2Function.class);
            List<TreeElement> children = new ArrayList<>(functions.size());
            for (Refal2Function fn : functions) {
                children.add(new Refal2StructureViewElement(fn));
            }
            return children.toArray(new TreeElement[0]);
        }
        return TreeElement.EMPTY_ARRAY;
    }
}
