package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalFile;
import com.github.refal5lambda.r2.Refal2File;
import com.github.refal5lambda.r2.Refal2Lexer;
import com.github.refal5lambda.r2.Refal2TokenTypes;
import com.intellij.lexer.Lexer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.search.IndexPatternBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Makes {@code TODO} / {@code FIXME} markers inside Refal comments light up in the editor and
 * appear in the TODO tool window — for both the Refal-5λ and Refal-2 dialects. Refal has only
 * line/asterisk comments, so the deltas are zero.
 */
public final class RefalIndexPatternBuilder implements IndexPatternBuilder {

    private static final TokenSet LAMBDA_COMMENTS =
            TokenSet.create(RefalTokenTypes.LINE_COMMENT, RefalTokenTypes.BLOCK_COMMENT);

    @Override
    public @Nullable Lexer getIndexingLexer(@NotNull PsiFile file) {
        if (file instanceof RefalFile) return new RefalLexer();
        if (file instanceof Refal2File) return new Refal2Lexer();
        return null;
    }

    @Override
    public @Nullable TokenSet getCommentTokenSet(@NotNull PsiFile file) {
        if (file instanceof RefalFile) return LAMBDA_COMMENTS;
        if (file instanceof Refal2File) return Refal2TokenTypes.COMMENTS;
        return null;
    }

    @Override
    public int getCommentStartDelta(IElementType tokenType) {
        return 0;
    }

    @Override
    public int getCommentEndDelta(IElementType tokenType) {
        return 0;
    }
}
