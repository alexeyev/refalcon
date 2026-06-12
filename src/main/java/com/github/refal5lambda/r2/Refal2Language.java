package com.github.refal5lambda.r2;

import com.intellij.lang.Language;

/** The Refal-2 dialect: same .ref file type, selected per file by {@link RefalDialectSubstitutor}. */
public final class Refal2Language extends Language {
    public static final Refal2Language INSTANCE = new Refal2Language();
    private Refal2Language() {
        super("Refal2");
    }

    @Override
    public String getDisplayName() {
        return "Refal-2";
    }
}
