package com.github.refal5lambda.r2;

import com.github.refal5lambda.RefalFileType;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

/** A .ref file recognized as the Refal-2 dialect by {@link RefalDialectSubstitutor}. */
public final class Refal2File extends PsiFileBase {
    public Refal2File(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, Refal2Language.INSTANCE);
    }

    @NotNull @Override
    public FileType getFileType() {
        return RefalFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Refal-2 file";
    }
}
