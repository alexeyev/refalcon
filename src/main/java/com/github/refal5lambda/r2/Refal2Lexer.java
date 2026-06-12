package com.github.refal5lambda.r2;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** {@link LexerBase} adapter over {@link Refal2Scanner}; carries the function-position state. */
public final class Refal2Lexer extends LexerBase {
    private CharSequence buffer = "";
    private int bufferEnd = 0;
    private int tokenStart = 0;
    private int tokenEnd = 0;
    private int state = Refal2Scanner.STATE_DEFAULT;
    private int nextState = Refal2Scanner.STATE_DEFAULT;
    private IElementType tokenType = null;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.bufferEnd = endOffset;
        this.tokenStart = startOffset;
        this.state = initialState;
        locateToken();
    }

    private void locateToken() {
        if (tokenStart >= bufferEnd) {
            tokenType = null;
            tokenEnd = tokenStart;
            nextState = state;
            return;
        }
        Refal2Scanner.Token t = Refal2Scanner.next(buffer, tokenStart, bufferEnd, state);
        tokenEnd = t.end;
        nextState = t.state;
        tokenType = t.kind == null ? TokenType.WHITE_SPACE : Refal2TokenTypes.forKind(t.kind);
    }

    @Override public int getState() { return state; }
    @Nullable @Override public IElementType getTokenType() { return tokenType; }
    @Override public int getTokenStart() { return tokenStart; }
    @Override public int getTokenEnd() { return tokenEnd; }

    @Override
    public void advance() {
        tokenStart = tokenEnd;
        state = nextState;
        locateToken();
    }

    @NotNull @Override public CharSequence getBufferSequence() { return buffer; }
    @Override public int getBufferEnd() { return bufferEnd; }
}
