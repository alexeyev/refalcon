package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalFunction;
import com.github.refal5lambda.r2.Refal2Function;
import com.github.refal5lambda.r2.Refal2Language;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Shows the enclosing function in the breadcrumb bar (and enables "navigate to enclosing
 * function"), so it stays obvious which function the caret is in while scrolling a long file.
 * Works for both the Refal-5λ and Refal-2 dialects.
 */
public final class RefalBreadcrumbsProvider implements BreadcrumbsProvider {

    private static final Language[] LANGUAGES = {
            RefalLanguage.INSTANCE, Refal2Language.INSTANCE,
    };

    @Override
    public Language[] getLanguages() {
        return LANGUAGES;
    }

    @Override
    public boolean acceptElement(@NotNull PsiElement element) {
        return element instanceof RefalFunction || element instanceof Refal2Function;
    }

    @Override
    public @NotNull String getElementInfo(@NotNull PsiElement element) {
        String name = element instanceof PsiNamedElement ? ((PsiNamedElement) element).getName() : null;
        return name == null || name.isEmpty() ? "\u2026" : name;
    }
}
