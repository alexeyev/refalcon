package com.github.refal5lambda.r2;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class Refal2StructureViewModel extends StructureViewModelBase
        implements StructureViewModel.ElementInfoProvider {

    Refal2StructureViewModel(@Nullable Editor editor, @NotNull PsiFile file) {
        super(file, editor, new Refal2StructureViewElement(file));
        withSuitableClasses(Refal2Function.class);
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return element.getValue() instanceof Refal2Function;
    }
}
