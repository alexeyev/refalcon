package com.github.refal5lambda;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/** Maps Refal token types to editor colors. All keys fall back to standard language colors. */
public final class RefalSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey LINE_COMMENT =
            createTextAttributesKey("REFAL_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT =
            createTextAttributesKey("REFAL_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey NATIVE_BLOCK =
            createTextAttributesKey("REFAL_NATIVE_BLOCK", DefaultLanguageHighlighterColors.DOC_COMMENT);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("REFAL_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey STRING_ESCAPE_VALID =
            createTextAttributesKey("REFAL_STRING_ESCAPE_VALID", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey STRING_ESCAPE_INVALID =
            createTextAttributesKey("REFAL_STRING_ESCAPE_INVALID", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("REFAL_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("REFAL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey BAD_DIRECTIVE =
            createTextAttributesKey("REFAL_BAD_DIRECTIVE", HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey S_VARIABLE =
            createTextAttributesKey("REFAL_S_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey T_VARIABLE =
            createTextAttributesKey("REFAL_T_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey E_VARIABLE =
            createTextAttributesKey("REFAL_E_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey FUNCTION_DEFINITION =
            createTextAttributesKey("REFAL_FUNCTION_DEFINITION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey FUNCTION_CALL =
            createTextAttributesKey("REFAL_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("REFAL_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey PARENTHESES =
            createTextAttributesKey("REFAL_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey BRACES =
            createTextAttributesKey("REFAL_BRACES", DefaultLanguageHighlighterColors.BRACES);
    // Activation brackets < > are Refal's evaluation operator, and in the default themes BRACES
    // renders as plain text — visually identical to variables (LOCAL_VARIABLE). Basing them on
    // KEYWORD makes them stand out in both IntelliJ Light and Darcula; re-tunable in the color page.
    public static final TextAttributesKey ANGLES =
            createTextAttributesKey("REFAL_ANGLES", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey BRACKETS =
            createTextAttributesKey("REFAL_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey SEMICOLON =
            createTextAttributesKey("REFAL_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey COMMA =
            createTextAttributesKey("REFAL_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey OPERATOR =
            createTextAttributesKey("REFAL_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    private static final Map<IElementType, TextAttributesKey[]> KEYS = new HashMap<>();

    private static void put(IElementType type, TextAttributesKey key) {
        KEYS.put(type, new TextAttributesKey[]{key});
    }

    static {
        put(RefalTokenTypes.LINE_COMMENT, LINE_COMMENT);
        put(RefalTokenTypes.BLOCK_COMMENT, BLOCK_COMMENT);
        put(RefalTokenTypes.NATIVE_BLOCK, NATIVE_BLOCK);
        put(RefalTokenTypes.STRING_SINGLE, STRING);
        put(RefalTokenTypes.STRING_DOUBLE, STRING);
        put(RefalTokenTypes.STRING_ESCAPE_VALID, STRING_ESCAPE_VALID);
        put(RefalTokenTypes.STRING_ESCAPE_INVALID, STRING_ESCAPE_INVALID);
        put(RefalTokenTypes.NUMBER, NUMBER);
        put(RefalTokenTypes.KEYWORD, KEYWORD);
        put(RefalTokenTypes.BAD_DIRECTIVE, BAD_DIRECTIVE);
        put(RefalTokenTypes.S_VARIABLE, S_VARIABLE);
        put(RefalTokenTypes.T_VARIABLE, T_VARIABLE);
        put(RefalTokenTypes.E_VARIABLE, E_VARIABLE);
        put(RefalTokenTypes.FUNCTION_DEFINITION, FUNCTION_DEFINITION);
        put(RefalTokenTypes.FUNCTION_CALL, FUNCTION_CALL);
        put(RefalTokenTypes.IDENTIFIER, IDENTIFIER);
        put(RefalTokenTypes.LPAREN, PARENTHESES);
        put(RefalTokenTypes.RPAREN, PARENTHESES);
        put(RefalTokenTypes.LBRACE, BRACES);
        put(RefalTokenTypes.RBRACE, BRACES);
        put(RefalTokenTypes.LANGLE, ANGLES);
        put(RefalTokenTypes.RANGLE, ANGLES);
        put(RefalTokenTypes.LBRACK, BRACKETS);
        put(RefalTokenTypes.RBRACK, BRACKETS);
        put(RefalTokenTypes.SEMICOLON, SEMICOLON);
        put(RefalTokenTypes.COMMA, COMMA);
        put(RefalTokenTypes.COLON, OPERATOR);
        put(RefalTokenTypes.EQ, OPERATOR);
        // OTHER intentionally has no mapping -> rendered with default text color.
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new RefalLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        TextAttributesKey[] keys = KEYS.get(tokenType);
        return keys != null ? keys : TextAttributesKey.EMPTY_ARRAY;
    }
}
