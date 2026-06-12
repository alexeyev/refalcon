package com.github.refal5lambda.r2;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** File structure popup (Ctrl/Cmd+F12) for Refal-2: lists the functions. */
public final class Refal2StructureViewFactory implements PsiStructureViewFactory {
    @Nullable @Override
    public StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
            @NotNull @Override
            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return new Refal2StructureViewModel(editor, psiFile);
            }
        };
    }
}
