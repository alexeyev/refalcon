package com.github.refal5lambda.r2;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.github.refal5lambda.r2.Refal2Scanner.Kind;
import static org.junit.Assert.assertEquals;

/**
 * Scanner tests built from REAL Refal-2 sources (refal2-0.2.3 distribution: hello.ref, fact.ref,
 * xcv.ref) — the snippets below are verbatim or minimally trimmed from those files.
 */
public class Refal2ScannerTest {

    private static List<Kind> kinds(String text) {
        List<Kind> out = new ArrayList<>();
        int pos = 0, state = Refal2Scanner.STATE_DEFAULT;
        while (pos < text.length()) {
            Refal2Scanner.Token t = Refal2Scanner.next(text, pos, text.length(), state);
            if (t.kind != null) out.add(t.kind);
            pos = t.end;
            state = t.state;
        }
        return out;
    }

    @Test
    public void moduleHeaderAndDirectives() {
        // hello.ref: "hello\tstart\n\tentry go\n\textrn print"
        assertEquals(List.of(Kind.DEF_NAME, Kind.KEYWORD,
                        Kind.KEYWORD, Kind.IDENT,
                        Kind.KEYWORD, Kind.IDENT),
                kinds("hello\tstart\n\tentry go\n\textrn print"));
    }

    @Test
    public void angleCallNamesAreFunctionPosition() {
        // hello.ref body
        assertEquals(List.of(Kind.DEF_NAME, Kind.EQ, Kind.LANGLE, Kind.FUNC, Kind.STRING, Kind.RANGLE),
                kinds("go  \t= <Print 'Hello World!'>"));
    }

    @Test
    public void kCallsNumbersAndStackedDots() {
        // xcv.ref: cvd0 EX = k/cvd1/ k/DRn/ (EX) /59//10144256/..
        assertEquals(List.of(Kind.DEF_NAME, Kind.E_VAR, Kind.EQ,
                        Kind.KOPEN, Kind.FUNC, Kind.KNAME_CLOSE,
                        Kind.KOPEN, Kind.FUNC, Kind.KNAME_CLOSE,
                        Kind.LPAREN, Kind.E_VAR, Kind.RPAREN,
                        Kind.NUMBER, Kind.NUMBER,
                        Kind.DOT, Kind.DOT),
                kinds("cvd0 EX = k/cvd1/ k/DRn/ (EX) /59//10144256/.."));
    }

    @Test
    public void variableChainsDecomposeButIdentifiersDoNot() {
        // xcv.ref pattern: s1s2s3s4 = four s-variables; 'symb' must stay one identifier
        assertEquals(List.of(Kind.DEF_NAME, Kind.S_VAR, Kind.S_VAR, Kind.S_VAR, Kind.S_VAR, Kind.EQ, Kind.IDENT),
                kinds("f s1s2s3s4 = symb"));
    }

    @Test
    public void specifierVariables() {
        // xcv.ref: CVB  '-' V(D)X = …
        assertEquals(List.of(Kind.DEF_NAME, Kind.STRING, Kind.V_VAR, Kind.EQ),
                kinds("CVB  '-' V(D)X ="));
    }

    @Test
    public void starCommentOnlyAtColumnZeroAndPlusContinuation() {
        assertEquals(List.of(Kind.COMMENT, Kind.DEF_NAME, Kind.EQ, Kind.NUMBER, Kind.PLUS),
                kinds("*  prowerka mul\nf1 = /1/ +"));
    }

    @Test
    public void caseInsensitiveKeywordsAndKOpen() {
        // test1.ref uses START/EXTRN uppercase and K/…/ uppercase
        assertEquals(List.of(Kind.DEF_NAME, Kind.KEYWORD, Kind.KEYWORD, Kind.IDENT,
                        Kind.DEF_NAME, Kind.EQ, Kind.KOPEN, Kind.FUNC, Kind.KNAME_CLOSE,
                        Kind.LPAREN, Kind.NUMBER, Kind.RPAREN, Kind.NUMBER, Kind.DOT),
                kinds("tes1 START\n    EXTRN add\ngo = K/prad/(/1/)/2/."));
    }
}
