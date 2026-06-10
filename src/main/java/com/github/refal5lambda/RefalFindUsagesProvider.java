package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalFunction;
import com.intellij.lang.HelpID;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Enables Find Usages for Refal function names (the word index treats names as identifiers). */
public final class RefalFindUsagesProvider implements FindUsagesProvider {

    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(
                new RefalLexer(),
                TokenSet.create(RefalTokenTypes.FUNCTION_DEFINITION, RefalTokenTypes.FUNCTION_CALL,
                        RefalTokenTypes.IDENTIFIER),
                TokenSet.create(RefalTokenTypes.LINE_COMMENT, RefalTokenTypes.BLOCK_COMMENT),
                TokenSet.create(RefalTokenTypes.STRING_SINGLE, RefalTokenTypes.STRING_DOUBLE));
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement element) {
        return element instanceof PsiNamedElement;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement element) {
        return HelpID.FIND_OTHER_USAGES;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        return element instanceof RefalFunction ? "function" : "";
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof PsiNamedElement) {
            String n = ((PsiNamedElement) element).getName();
            if (n != null) return n;
        }
        return element.getText();
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        return getDescriptiveName(element);
    }
}
