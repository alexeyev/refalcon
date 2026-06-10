package com.github.refal5lambda;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Pure unit tests for the IntelliJ-free {@link RefalScanner}. */
public class RefalScannerTest {

    private static final class Tok {
        final RefalTokenKind kind;
        final String text;
        Tok(RefalTokenKind kind, String text) { this.kind = kind; this.text = text; }
        @Override public String toString() { return kind + "[" + text + "]"; }
    }

    /** Drive the stateful scanner over the whole input, asserting it always advances. */
    private static List<Tok> lex(String src) {
        List<Tok> out = new ArrayList<>();
        int pos = 0, end = src.length(), state = RefalScanner.STATE_DEFAULT, guard = 0;
        while (pos < end) {
            RefalScanner.Token t = RefalScanner.next(src, pos, end, state);
            assertTrue("scanner must advance at offset " + pos, t.end > pos);
            out.add(new Tok(t.kind, src.substring(pos, t.end)));
            pos = t.end;
            state = t.state;
            assertTrue("runaway scan", ++guard < 100000);
        }
        assertEquals("scanner must end back in the default state", RefalScanner.STATE_DEFAULT, state);
        return out;
    }

    private static List<Tok> lexNoWs(String src) {
        List<Tok> r = new ArrayList<>();
        for (Tok t : lex(src)) if (t.kind != RefalTokenKind.WHITE_SPACE) r.add(t);
        return r;
    }

    @Test
    public void lineCommentOnlyAtColumnZero() {
        List<Tok> t = lexNoWs("*comment\n  *notcomment\n");
        assertEquals(RefalTokenKind.LINE_COMMENT, t.get(0).kind);
        assertEquals("*comment", t.get(0).text);
        // the indented star must NOT be swallowed as a line comment
        assertFalse(t.stream().anyMatch(x -> x.kind == RefalTokenKind.LINE_COMMENT
                && x.text.contains("notcomment")));
    }

    @Test
    public void blockAndNativeBlocks() {
        assertEquals(RefalTokenKind.BLOCK_COMMENT, lexNoWs("/* a b */").get(0).kind);
        assertEquals(RefalTokenKind.NATIVE_BLOCK, lexNoWs("%% int x; %%").get(0).kind);
    }

    @Test
    public void directivesKnownVsUnknown() {
        assertEquals(RefalTokenKind.KEYWORD, lexNoWs("$ENTRY").get(0).kind);
        assertEquals(RefalTokenKind.KEYWORD, lexNoWs("$EXTERN").get(0).kind);
        assertEquals(RefalTokenKind.BAD_DIRECTIVE, lexNoWs("$FOOBAR").get(0).kind);
    }

    @Test
    public void variables() {
        assertEquals(RefalTokenKind.S_VARIABLE, lexNoWs("s.N").get(0).kind);
        assertEquals(RefalTokenKind.T_VARIABLE, lexNoWs("t.Term").get(0).kind);
        assertEquals(RefalTokenKind.E_VARIABLE, lexNoWs("e.Rest").get(0).kind);
    }

    @Test
    public void number() {
        assertEquals(RefalTokenKind.NUMBER, lexNoWs("12345").get(0).kind);
    }

    @Test
    public void functionDefinitionVsCall() {
        assertEquals(RefalTokenKind.FUNCTION_DEFINITION, lexNoWs("Go {").get(0).kind);
        List<Tok> call = lexNoWs("<Fact 5>");
        assertEquals(RefalTokenKind.LANGLE, call.get(0).kind);
        assertEquals(RefalTokenKind.FUNCTION_CALL, call.get(1).kind);
        assertEquals(RefalTokenKind.IDENTIFIER, lexNoWs("Foo").get(0).kind);
    }

    @Test
    public void brackets() {
        List<Tok> t = lexNoWs("(){}<>[]");
        RefalTokenKind[] expected = {
                RefalTokenKind.LPAREN, RefalTokenKind.RPAREN,
                RefalTokenKind.LBRACE, RefalTokenKind.RBRACE,
                RefalTokenKind.LANGLE, RefalTokenKind.RANGLE,
                RefalTokenKind.LBRACK, RefalTokenKind.RBRACK
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals("bracket #" + i, expected[i], t.get(i).kind);
        }
    }

    @Test
    public void singleQuotedStringEscapes() {
        // 'a\n\q\x41\x' : valid \n, invalid \q, valid \x41, invalid \x
        List<Tok> t = lexNoWs("'a\\n\\q\\x41\\x'");
        assertEquals(RefalTokenKind.STRING_SINGLE, t.get(0).kind);
        assertEquals("'", t.get(0).text);
        assertTrue(has(t, RefalTokenKind.STRING_ESCAPE_VALID, "\\n"));
        assertTrue(has(t, RefalTokenKind.STRING_ESCAPE_INVALID, "\\q"));
        assertTrue(has(t, RefalTokenKind.STRING_ESCAPE_VALID, "\\x41"));
        assertTrue(has(t, RefalTokenKind.STRING_ESCAPE_INVALID, "\\x"));
        assertEquals(RefalTokenKind.STRING_SINGLE, t.get(t.size() - 1).kind);
        assertEquals("'", t.get(t.size() - 1).text);
    }

    @Test
    public void escapedQuoteDoesNotCloseDoubleString() {
        // "a\"b" : the \" is an escape; only the first and last " are delimiters
        List<Tok> t = lexNoWs("\"a\\\"b\"");
        long quotes = t.stream()
                .filter(x -> x.kind == RefalTokenKind.STRING_DOUBLE && x.text.equals("\""))
                .count();
        assertEquals("only opening and closing quotes", 2, quotes);
        assertTrue(has(t, RefalTokenKind.STRING_ESCAPE_VALID, "\\\""));
    }

    @Test
    public void unterminatedStringResetsAtNewline() {
        // string never closes; the newline must reset state so the next line parses normally
        List<Tok> t = lexNoWs("'oops\nFoo {");
        assertEquals(RefalTokenKind.FUNCTION_DEFINITION, t.get(t.size() - 2).kind);
        assertEquals(RefalTokenKind.LBRACE, t.get(t.size() - 1).kind);
    }

    @Test
    public void tokensCoverWholeInput() {
        String src = "* c\n$ENTRY Go {\n  = <Prout 'Hi, ' e.X '!\\n'>;\n}\n/* b */\n%% native %%\n";
        int pos = 0, end = src.length(), state = RefalScanner.STATE_DEFAULT;
        StringBuilder rebuilt = new StringBuilder();
        while (pos < end) {
            RefalScanner.Token t = RefalScanner.next(src, pos, end, state);
            rebuilt.append(src, pos, t.end);
            pos = t.end;
            state = t.state;
        }
        assertEquals("tokens must exactly tile the input", src, rebuilt.toString());
    }

    private static boolean has(List<Tok> tokens, RefalTokenKind kind, String text) {
        return tokens.stream().anyMatch(x -> x.kind == kind && x.text.equals(text));
    }
}
