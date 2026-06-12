package com.github.refal5lambda.run;

import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * The classic Turkish-I pitfall: {@code "WARNING".toLowerCase()} depends on the default locale
 * (dotless ı), so severity normalization must use {@link Locale#ROOT}. This test runs the parser
 * under tr-TR to pin that.
 */
public class RefalDiagnosticLocaleTest {

    @Test
    public void severityNormalizationIsLocaleIndependent() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            List<RefalDiagnosticParser.Diagnostic> list =
                    RefalDiagnosticParser.parse("c1.ref:3:1: WARNING: suspicious pattern\n"
                            + "c1.ref:5:2: NOTE: see above\n");
            assertEquals(2, list.size());
            assertEquals("warning", list.get(0).severity);
            assertEquals("note", list.get(1).severity);
        } finally {
            Locale.setDefault(saved);
        }
    }
}
