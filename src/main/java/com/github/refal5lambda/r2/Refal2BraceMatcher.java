package com.github.refal5lambda.r2;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Pairs ( ), &lt; &gt;, and the Refal-2 concretization brackets k/ … . */
public final class Refal2BraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = {
            new BracePair(Refal2TokenTypes.LPAREN, Refal2TokenTypes.RPAREN, false),
            new BracePair(Refal2TokenTypes.LANGLE, Refal2TokenTypes.RANGLE, false),
            new BracePair(Refal2TokenTypes.KOPEN, Refal2TokenTypes.DOT, false),
    };

    @Override
    public BracePair @NotNull [] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
