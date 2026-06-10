package com.github.refal5lambda.run;

import com.github.refal5lambda.run.RefalDiagnosticParser.Diagnostic;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link RefalDiagnosticParser}. The sample lines are REAL output captured from the
 * Refal-5λ compiler ({@code rlc}, bootstrapped from bmstu-iu9/simple-refal-distrib): diagnostics are
 * printed on stderr as {@code file:line:col: ERROR: message}.
 */
public class RefalDiagnosticParserTest {

    @Test
    public void parsesRealSyntaxError() {
        List<Diagnostic> d = RefalDiagnosticParser.parse(
                "*Compiling c1.ref:\nc1.ref:3:1: ERROR: Missed '>'\n");
        assertEquals(1, d.size());                 // the "*Compiling ..." line is ignored
        assertEquals(3, d.get(0).line);
        assertEquals(1, d.get(0).column);
        assertEquals("error", d.get(0).severity);  // "ERROR" normalised to "error"
        assertEquals("Missed '>'", d.get(0).message);
    }

    @Test
    public void parsesRealSemanticError() {
        List<Diagnostic> d = RefalDiagnosticParser.parse(
                "c3.ref:2:5: ERROR: Function ThisFunctionDoesNotExist is not defined\n");
        assertEquals(1, d.size());
        assertEquals(2, d.get(0).line);
        assertEquals(5, d.get(0).column);
        assertTrue(d.get(0).message.contains("is not defined"));
    }

    @Test
    public void parsesMultipleRealDiagnostics() {
        List<Diagnostic> d = RefalDiagnosticParser.parse(
                "c4.ref:2:5: ERROR: Bad character '@'\n"
                        + "c4.ref:2:6: ERROR: Bad character '@'\n"
                        + "c4.ref:2:7: ERROR: Bad character '@'\n");
        assertEquals(3, d.size());
        assertEquals(5, d.get(0).column);
        assertEquals(7, d.get(2).column);
    }

    @Test
    public void ignoresNonRefalAndChatterLines() {
        // progress text and C++ diagnostics must not become Refal annotations
        List<Diagnostic> d = RefalDiagnosticParser.parse(
                "*Compiling x.ref:\n** Compilation succeeded **\ncheck.cpp:12:3: error: from generated code\n");
        assertTrue(d.isEmpty());
    }

    @Test
    public void toleratesDiagnosticWithoutColumn() {
        // rlc always emits a column, but the parser tolerates a column-less "file:line: message" too
        List<Diagnostic> d = RefalDiagnosticParser.parse("hello.ref:10: something went wrong\n");
        assertEquals(1, d.size());
        assertEquals(10, d.get(0).line);
        assertEquals(0, d.get(0).column);
    }
}
