package com.github.refal5lambda.r2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Hand-written scanner for the Refal-2 dialect. No IntelliJ imports (unit-testable standalone).
 *
 * <p>Grounded in the original refal2-0.2.3 distribution (refal.net/~belous): its test programs
 * ({@code hello.ref}, {@code fact.ref}, {@code test*.ref}) and interpreter sources
 * ({@code xcv.ref}). The notation differs sharply from Refal-5λ:
 * <ul>
 *   <li>modules: {@code name start} … {@code end}; directives {@code entry}/{@code extrn}
 *       (no {@code $}), case-insensitive;</li>
 *   <li>functions are defined by an identifier at <b>column 0</b>, no braces, no semicolons;
 *       {@code +} at end of line continues a sentence;</li>
 *   <li>calls: {@code <Name args>} or {@code k/name/ args.} — a single {@code .} closes one
 *       call, so {@code ..} closes two nested ones;</li>
 *   <li>numbers (macrodigits) are written {@code /123/};</li>
 *   <li>variables are a type letter (S, W, V, E — case-insensitive) plus a single index
 *       character, optionally with a specifier: {@code e1}, {@code s1s2s3}, {@code EX},
 *       {@code V(D)X}. Names are case-insensitive ({@code extrn print} … {@code <Print …>}).</li>
 * </ul>
 *
 * <p>Identifier-vs-variable disambiguation is positional, as in the original language: a word in
 * <i>function position</i> (column 0, right after {@code <}, or inside {@code k/…/}) is a name;
 * elsewhere an alphanumeric run is a variable chain iff it fully decomposes into
 * (type-letter, index-char) pairs — {@code s1s2s3s4} → four variables, {@code symb} → identifier.
 */
public final class Refal2Scanner {

    private Refal2Scanner() {}

    public static final int STATE_DEFAULT = 0;
    public static final int STATE_AFTER_LANGLE = 1;   // next word is a function name: <Name …
    public static final int STATE_K_NAME = 2;         // inside k/…: the word is a function name
    public static final int STATE_K_NAME_END = 3;     // function name consumed, expect closing '/'

    public enum Kind {
        COMMENT, STRING, NUMBER, KEYWORD,
        IDENT, DEF_NAME, FUNC,
        S_VAR, W_VAR, V_VAR, E_VAR,
        LANGLE, RANGLE, KOPEN, KNAME_CLOSE, DOT,
        LPAREN, RPAREN, EQ, PLUS, COMMA, SLASH, BAD
    }

    public static final class Token {
        public final Kind kind;
        public final int end;
        public final int state;
        public Token(Kind kind, int end, int state) {
            this.kind = kind; this.end = end; this.state = state;
        }
    }

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "START", "END", "ENTRY", "EXTRN", "EMPTY", "SWAP"));

    private static boolean isTypeLetter(char c) {
        return c == 's' || c == 'S' || c == 'w' || c == 'W'
                || c == 'v' || c == 'V' || c == 'e' || c == 'E';
    }

    private static boolean isIdentStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static boolean isIdentPart(char c) {
        return isIdentStart(c) || (c >= '0' && c <= '9');
    }

    public static Token next(CharSequence text, int start, int end, int state) {
        char c = text.charAt(start);

        // Whitespace (newline resets the call-name states defensively).
        if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
            int i = start;
            while (i < end) {
                char w = text.charAt(i);
                if (w != ' ' && w != '\t' && w != '\r' && w != '\n') break;
                i++;
            }
            return new Token(null0(), i, state);
        }

        boolean atLineStart = start == 0 || text.charAt(start - 1) == '\n';

        // '*' is a comment only at column 0 (same convention as Refal-5/5λ).
        if (c == '*' && atLineStart) {
            int i = start;
            while (i < end && text.charAt(i) != '\n') i++;
            return new Token(Kind.COMMENT, i, STATE_DEFAULT);
        }

        // Strings: '...' (kept single-line; the historic sources never wrap them).
        if (c == '\'') {
            int i = start + 1;
            while (i < end && text.charAt(i) != '\'' && text.charAt(i) != '\n') i++;
            if (i < end && text.charAt(i) == '\'') i++;
            return new Token(Kind.STRING, i, state);
        }

        // Closing '/' of k/name/.
        if (c == '/' && state == STATE_K_NAME_END) {
            return new Token(Kind.KNAME_CLOSE, start + 1, STATE_DEFAULT);
        }

        // /123/ macrodigit numbers.
        if (c == '/') {
            int i = start + 1;
            while (i < end && text.charAt(i) >= '0' && text.charAt(i) <= '9') i++;
            if (i > start + 1 && i < end && text.charAt(i) == '/') {
                return new Token(Kind.NUMBER, i + 1, state);
            }
            return new Token(Kind.SLASH, start + 1, state);
        }

        // k/ opens a concretization call.
        if ((c == 'k' || c == 'K') && start + 1 < end && text.charAt(start + 1) == '/') {
            return new Token(Kind.KOPEN, start + 2, STATE_K_NAME);
        }

        switch (c) {
            case '<': return new Token(Kind.LANGLE, start + 1, STATE_AFTER_LANGLE);
            case '>': return new Token(Kind.RANGLE, start + 1, STATE_DEFAULT);
            case '.': return new Token(Kind.DOT, start + 1, STATE_DEFAULT);
            case '(': return new Token(Kind.LPAREN, start + 1, STATE_DEFAULT);
            case ')': return new Token(Kind.RPAREN, start + 1, STATE_DEFAULT);
            case '=': return new Token(Kind.EQ, start + 1, STATE_DEFAULT);
            case '+': return new Token(Kind.PLUS, start + 1, STATE_DEFAULT);
            case ',': return new Token(Kind.COMMA, start + 1, STATE_DEFAULT);
            default:
        }

        if (isIdentStart(c)) {
            // Specifier variable: V(D)X — type letter, '(' spec ')' and one index character.
            if (isTypeLetter(c) && state != STATE_AFTER_LANGLE && state != STATE_K_NAME
                    && start + 1 < end && text.charAt(start + 1) == '(') {
                int i = start + 2;
                while (i < end && isIdentPart(text.charAt(i))) i++;
                if (i > start + 2 && i + 1 < end && text.charAt(i) == ')' && isIdentPart(text.charAt(i + 1))) {
                    return new Token(varKind(c), i + 2, state);
                }
            }

            int i = start;
            while (i < end && isIdentPart(text.charAt(i))) i++;
            int len = i - start;
            String word = text.subSequence(start, i).toString();

            if (KEYWORDS.contains(word.toUpperCase(java.util.Locale.ROOT))) {
                return new Token(Kind.KEYWORD, i, STATE_DEFAULT);
            }
            if (state == STATE_K_NAME) {
                return new Token(Kind.FUNC, i, STATE_K_NAME_END);
            }
            if (state == STATE_AFTER_LANGLE) {
                return new Token(Kind.FUNC, i, STATE_DEFAULT);
            }
            if (atLineStart) {
                return new Token(Kind.DEF_NAME, i, STATE_DEFAULT);
            }
            // Variable chain: the whole run must decompose into (type letter, index char) pairs.
            if (len % 2 == 0 && isTypeLetter(c)) {
                boolean decomposes = true;
                for (int p = start; p < i; p += 2) {
                    if (!isTypeLetter(text.charAt(p))) { decomposes = false; break; }
                }
                if (decomposes) {
                    return new Token(varKind(c), start + 2, state);   // first pair; rest re-lexed
                }
            }
            return new Token(Kind.IDENT, i, STATE_DEFAULT);
        }

        return new Token(Kind.BAD, start + 1, STATE_DEFAULT);
    }

    private static Kind varKind(char typeLetter) {
        switch (Character.toUpperCase(typeLetter)) {
            case 'S': return Kind.S_VAR;
            case 'W': return Kind.W_VAR;
            case 'V': return Kind.V_VAR;
            default:  return Kind.E_VAR;
        }
    }

    /** Whitespace pseudo-kind: callers map a null kind to the platform's WHITE_SPACE. */
    private static Kind null0() {
        return null;
    }
}
