package com.github.refal5lambda.r2;

import com.github.refal5lambda.RefalSyntaxHighlighter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Highlighting for Refal-2, reusing the Refal-5λ color keys — both dialects share one page in
 * Settings → Editor → Color Scheme. W-variables (terms) map to the t-variable key, V (non-empty
 * expression) to the e-variable key; {@code k/} and the closing {@code .} use the activation
 * bracket color, like {@code < >}.
 */
public final class Refal2SyntaxHighlighter extends SyntaxHighlighterBase {

    private static final Map<IElementType, TextAttributesKey> KEYS = new HashMap<>();

    private static void map(Refal2Scanner.Kind kind, TextAttributesKey key) {
        KEYS.put(Refal2TokenTypes.forKind(kind), key);
    }

    static {
        map(Refal2Scanner.Kind.COMMENT, RefalSyntaxHighlighter.LINE_COMMENT);
        map(Refal2Scanner.Kind.STRING, RefalSyntaxHighlighter.STRING);
        map(Refal2Scanner.Kind.NUMBER, RefalSyntaxHighlighter.NUMBER);
        map(Refal2Scanner.Kind.KEYWORD, RefalSyntaxHighlighter.KEYWORD);
        map(Refal2Scanner.Kind.DEF_NAME, RefalSyntaxHighlighter.FUNCTION_DEFINITION);
        map(Refal2Scanner.Kind.FUNC, RefalSyntaxHighlighter.FUNCTION_CALL);
        map(Refal2Scanner.Kind.IDENT, RefalSyntaxHighlighter.IDENTIFIER);
        map(Refal2Scanner.Kind.S_VAR, RefalSyntaxHighlighter.S_VARIABLE);
        map(Refal2Scanner.Kind.W_VAR, RefalSyntaxHighlighter.T_VARIABLE);
        map(Refal2Scanner.Kind.V_VAR, RefalSyntaxHighlighter.E_VARIABLE);
        map(Refal2Scanner.Kind.E_VAR, RefalSyntaxHighlighter.E_VARIABLE);
        map(Refal2Scanner.Kind.LANGLE, RefalSyntaxHighlighter.ANGLES);
        map(Refal2Scanner.Kind.RANGLE, RefalSyntaxHighlighter.ANGLES);
        map(Refal2Scanner.Kind.KOPEN, RefalSyntaxHighlighter.ANGLES);
        map(Refal2Scanner.Kind.KNAME_CLOSE, RefalSyntaxHighlighter.ANGLES);
        map(Refal2Scanner.Kind.DOT, RefalSyntaxHighlighter.ANGLES);
        map(Refal2Scanner.Kind.LPAREN, RefalSyntaxHighlighter.PARENTHESES);
        map(Refal2Scanner.Kind.RPAREN, RefalSyntaxHighlighter.PARENTHESES);
        map(Refal2Scanner.Kind.EQ, RefalSyntaxHighlighter.OPERATOR);
        map(Refal2Scanner.Kind.PLUS, RefalSyntaxHighlighter.OPERATOR);
        map(Refal2Scanner.Kind.SLASH, RefalSyntaxHighlighter.OPERATOR);
        map(Refal2Scanner.Kind.COMMA, RefalSyntaxHighlighter.COMMA);
        map(Refal2Scanner.Kind.BAD, HighlighterColors.BAD_CHARACTER);
    }

    @NotNull @Override
    public Lexer getHighlightingLexer() {
        return new Refal2Lexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return pack(KEYS.get(tokenType));
    }

    public static final class Factory extends SyntaxHighlighterFactory {
        @NotNull @Override
        public com.intellij.openapi.fileTypes.SyntaxHighlighter getSyntaxHighlighter(
                Project project, VirtualFile virtualFile) {
            return new Refal2SyntaxHighlighter();
        }
    }
}
