package com.github.refal5lambda;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** {@link LexerBase} adapter over {@link RefalScanner}. Carries the scanner's string-state. */
public final class RefalLexer extends LexerBase {
    private CharSequence buffer = "";
    private int bufferEnd = 0;
    private int tokenStart = 0;
    private int tokenEnd = 0;
    private int state = RefalScanner.STATE_DEFAULT;      // state at tokenStart (what getState returns)
    private int nextState = RefalScanner.STATE_DEFAULT;  // state after the current token
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
        RefalScanner.Token t = RefalScanner.next(buffer, tokenStart, bufferEnd, state);
        tokenEnd = t.end;
        nextState = t.state;
        tokenType = RefalTokenTypes.forKind(t.kind);
    }

    @Override
    public int getState() {
        return state;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        return tokenStart;
    }

    @Override
    public int getTokenEnd() {
        return tokenEnd;
    }

    @Override
    public void advance() {
        tokenStart = tokenEnd;
        state = nextState;
        locateToken();
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return bufferEnd;
    }
}
