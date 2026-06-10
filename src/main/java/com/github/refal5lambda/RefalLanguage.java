package com.github.refal5lambda;

import com.intellij.lang.Language;

/** The Refal-5 Lambda language definition. */
public final class RefalLanguage extends Language {
    public static final RefalLanguage INSTANCE = new RefalLanguage();

    private RefalLanguage() {
        super("Refal5Lambda");
    }

    @Override
    public String getDisplayName() {
        return "Refal-5\u03bb";
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}
