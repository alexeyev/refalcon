package com.github.refal5lambda;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Hand-written scanner for Refal-5 Lambda.
 *
 * <p>Mostly stateless: outside of string literals every token starts in {@link #STATE_DEFAULT}.
 * String literals are tokenised across several tokens (opening quote, text runs, escape
 * sequences, closing quote) so escapes can be highlighted; this needs the two extra states
 * {@link #STATE_SINGLE_STRING} / {@link #STATE_DOUBLE_STRING}. A newline always returns the
 * scanner to {@link #STATE_DEFAULT}, so string state never crosses a line and incremental
 * re-lexing stays cheap and correct.
 *
 * <p>No IntelliJ Platform imports here on purpose, so the logic is unit-testable in isolation.
 */
public final class RefalScanner {

    private RefalScanner() {}

    public static final int STATE_DEFAULT = 0;
    public static final int STATE_SINGLE_STRING = 1;
    public static final int STATE_DOUBLE_STRING = 2;

    /** Result of scanning one token: its {@code kind}, exclusive {@code end} offset, and the state afterwards. */
    public static final class Token {
        public final RefalTokenKind kind;
        public final int end;
        public final int state;
        public Token(RefalTokenKind kind, int end, int state) {
            this.kind = kind;
            this.end = end;
            this.state = state;
        }
    }

    private static final Set<String> DIRECTIVES = new HashSet<>(Arrays.asList(
            "ENTRY", "EXTERN", "EXTRN", "EXTERNAL", "EASTEREGG", "ENUM", "EENUM",
            "SWAP", "ESWAP", "SCOPEID", "DRIVE", "INLINE", "SPEC", "INCLUDE"));

    private static boolean isIdentStart(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_';
    }
    private static boolean isIdentPart(char c) {
        return isIdentStart(c) || (c >= '0' && c <= '9') || c == '-';
    }
    private static boolean isVarPart(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
                || (c >= '0' && c <= '9') || c == '_' || c == '-';
    }
    private static boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private static boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
    private static boolean isSimpleEscape(char c) {
        return c == 'n' || c == 'r' || c == 't' || c == '\\' || c == '\''
                || c == '"' || c == '<' || c == '>' || c == '(' || c == ')';
    }
    private static boolean isSpace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f';
    }

    /**
     * Scans one token from {@code text} starting at {@code start} (which must be {@code < end}),
     * given the incoming {@code state}.
     */
    public static Token next(CharSequence text, int start, int end, int state) {
        if (state == STATE_SINGLE_STRING) return insideString(text, start, end, '\'', STATE_SINGLE_STRING);
        if (state == STATE_DOUBLE_STRING) return insideString(text, start, end, '"', STATE_DOUBLE_STRING);
        return def(text, start, end);
    }

    /** Scanning in the default (non-string) state. */
    private static Token def(CharSequence text, int start, int end) {
        char c = text.charAt(start);

        if (isSpace(c)) {
            int i = start + 1;
            while (i < end && isSpace(text.charAt(i))) i++;
            return tok(RefalTokenKind.WHITE_SPACE, i);
        }

        // line comment: '*' as the first character of a line
        if (c == '*' && (start == 0 || text.charAt(start - 1) == '\n')) {
            int i = start + 1;
            while (i < end && text.charAt(i) != '\n') i++;
            return tok(RefalTokenKind.LINE_COMMENT, i);
        }

        // block comment /* ... */
        if (c == '/' && start + 1 < end && text.charAt(start + 1) == '*') {
            int i = start + 2;
            boolean closed = false;
            while (i + 1 < end) {
                if (text.charAt(i) == '*' && text.charAt(i + 1) == '/') { i += 2; closed = true; break; }
                i++;
            }
            if (!closed) i = end;
            return tok(RefalTokenKind.BLOCK_COMMENT, i);
        }

        // embedded native block %% ... %%
        if (c == '%' && start + 1 < end && text.charAt(start + 1) == '%') {
            int i = start + 2;
            boolean closed = false;
            while (i + 1 < end) {
                if (text.charAt(i) == '%' && text.charAt(i + 1) == '%') { i += 2; closed = true; break; }
                i++;
            }
            if (!closed) i = end;
            return tok(RefalTokenKind.NATIVE_BLOCK, i);
        }

        // opening quotes -> switch into a string state (escapes get their own tokens)
        if (c == '\'') return new Token(RefalTokenKind.STRING_SINGLE, start + 1, STATE_SINGLE_STRING);
        if (c == '"')  return new Token(RefalTokenKind.STRING_DOUBLE, start + 1, STATE_DOUBLE_STRING);

        // number (macrodigit)
        if (isDigit(c)) {
            int i = start + 1;
            while (i < end && isDigit(text.charAt(i))) i++;
            return tok(RefalTokenKind.NUMBER, i);
        }

        // directive $WORD
        if (c == '$') {
            int i = start + 1;
            while (i < end && isIdentPart(text.charAt(i))) i++;
            String word = text.subSequence(start + 1, i).toString();
            return tok(DIRECTIVES.contains(word) ? RefalTokenKind.KEYWORD : RefalTokenKind.BAD_DIRECTIVE, i);
        }

        // variable: s. / t. / e.  followed by at least one var char
        if ((c == 's' || c == 't' || c == 'e')
                && start + 2 < end
                && text.charAt(start + 1) == '.'
                && isVarPart(text.charAt(start + 2))) {
            int i = start + 2;
            while (i < end && isVarPart(text.charAt(i))) i++;
            RefalTokenKind k = (c == 's') ? RefalTokenKind.S_VARIABLE
                    : (c == 't') ? RefalTokenKind.T_VARIABLE
                    : RefalTokenKind.E_VARIABLE;
            return tok(k, i);
        }

        // identifier / function name
        if (isIdentStart(c)) {
            int i = start + 1;
            while (i < end && isIdentPart(text.charAt(i))) i++;
            if (start > 0) {
                char p = text.charAt(start - 1);
                if (p == '<' || p == '[') return tok(RefalTokenKind.FUNCTION_CALL, i);
            }
            int j = i;
            while (j < end && (text.charAt(j) == ' ' || text.charAt(j) == '\t')) j++;
            if (j < end && text.charAt(j) == '{') return tok(RefalTokenKind.FUNCTION_DEFINITION, i);
            return tok(RefalTokenKind.IDENTIFIER, i);
        }

        switch (c) {
            case '(': return tok(RefalTokenKind.LPAREN, start + 1);
            case ')': return tok(RefalTokenKind.RPAREN, start + 1);
            case '{': return tok(RefalTokenKind.LBRACE, start + 1);
            case '}': return tok(RefalTokenKind.RBRACE, start + 1);
            case '<': return tok(RefalTokenKind.LANGLE, start + 1);
            case '>': return tok(RefalTokenKind.RANGLE, start + 1);
            case '[': return tok(RefalTokenKind.LBRACK, start + 1);
            case ']': return tok(RefalTokenKind.RBRACK, start + 1);
            case ';': return tok(RefalTokenKind.SEMICOLON, start + 1);
            case ',': return tok(RefalTokenKind.COMMA, start + 1);
            case ':': return tok(RefalTokenKind.COLON, start + 1);
            case '=': return tok(RefalTokenKind.EQ, start + 1);
            default:  return tok(RefalTokenKind.OTHER, start + 1);
        }
    }

    /** Scanning while inside a {@code quote}-delimited string literal. */
    private static Token insideString(CharSequence text, int start, int end, char quote, int state) {
        char c = text.charAt(start);

        // newline ends an unterminated string and resets to default state
        if (c == '\n') {
            return new Token(RefalTokenKind.WHITE_SPACE, start + 1, STATE_DEFAULT);
        }

        // closing quote
        if (c == quote) {
            RefalTokenKind k = (quote == '\'') ? RefalTokenKind.STRING_SINGLE : RefalTokenKind.STRING_DOUBLE;
            return new Token(k, start + 1, STATE_DEFAULT);
        }

        // escape sequence
        if (c == '\\') {
            if (start + 1 >= end) {
                return new Token(RefalTokenKind.STRING_ESCAPE_INVALID, end, state); // lone backslash
            }
            char e = text.charAt(start + 1);
            if (e == 'x') {
                if (start + 3 < end && isHex(text.charAt(start + 2)) && isHex(text.charAt(start + 3))) {
                    return new Token(RefalTokenKind.STRING_ESCAPE_VALID, start + 4, state);
                }
                return new Token(RefalTokenKind.STRING_ESCAPE_INVALID, Math.min(start + 2, end), state);
            }
            if (isSimpleEscape(e)) {
                return new Token(RefalTokenKind.STRING_ESCAPE_VALID, start + 2, state);
            }
            return new Token(RefalTokenKind.STRING_ESCAPE_INVALID, start + 2, state);
        }

        // a run of ordinary string characters
        RefalTokenKind k = (quote == '\'') ? RefalTokenKind.STRING_SINGLE : RefalTokenKind.STRING_DOUBLE;
        int i = start;
        while (i < end) {
            char d = text.charAt(i);
            if (d == quote || d == '\\' || d == '\n') break;
            i++;
        }
        return new Token(k, i, state);
    }

    private static Token tok(RefalTokenKind kind, int end) {
        return new Token(kind, end, STATE_DEFAULT);
    }
}
