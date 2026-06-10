package com.github.refal5lambda;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RefalBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[]{
            new BracePair(RefalTokenTypes.LBRACE, RefalTokenTypes.RBRACE, true),
            new BracePair(RefalTokenTypes.LPAREN, RefalTokenTypes.RPAREN, false),
            new BracePair(RefalTokenTypes.LANGLE, RefalTokenTypes.RANGLE, false),
            new BracePair(RefalTokenTypes.LBRACK, RefalTokenTypes.RBRACK, false),
    };

    @Override
    public BracePair[] getPairs() {
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
