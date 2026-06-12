package com.github.refal5lambda.r2;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Word-index-backed usages for Refal-2 functions. */
public final class Refal2FindUsagesProvider implements FindUsagesProvider {

    @Nullable @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new Refal2Lexer(),
                Refal2TokenTypes.IDENTIFIERS, Refal2TokenTypes.COMMENTS, Refal2TokenTypes.STRINGS);
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement element) {
        return element instanceof Refal2Function;
    }

    @Nullable @Override
    public String getHelpId(@NotNull PsiElement element) {
        return null;
    }

    @NotNull @Override
    public String getType(@NotNull PsiElement element) {
        return "function";
    }

    @NotNull @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        String name = element instanceof PsiNamedElement ? ((PsiNamedElement) element).getName() : null;
        return name == null ? "" : name;
    }

    @NotNull @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        return getDescriptiveName(element);
    }
}
