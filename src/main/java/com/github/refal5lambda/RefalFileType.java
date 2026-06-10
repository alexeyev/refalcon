package com.github.refal5lambda;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/** Registers {@code .ref} / {@code .refi} as Refal-5 Lambda files. */
public final class RefalFileType extends LanguageFileType {
    public static final RefalFileType INSTANCE = new RefalFileType();

    private RefalFileType() {
        super(RefalLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        // Must stay in sync with the <fileType name="..."> in plugin.xml
        return "Refal-5 Lambda";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Refal-5\u03bb source file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "ref";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return RefalIcons.FILE;
    }
}
