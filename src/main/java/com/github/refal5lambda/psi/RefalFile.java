package com.github.refal5lambda.psi;

import com.github.refal5lambda.RefalFileType;
import com.github.refal5lambda.RefalLanguage;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public final class RefalFile extends PsiFileBase {
    public RefalFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, RefalLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return RefalFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Refal-5 Lambda File";
    }
}
