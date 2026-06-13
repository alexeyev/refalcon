package com.github.refal5lambda.r2;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiReference;
import com.github.refal5lambda.RefalIcons;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

/**
 * Dialect detection and Refal-2 navigation. All Refal-2 snippets are modeled on the real
 * refal2-0.2.3 sources (hello.ref, xcv.ref): name-start headers, bare entry/extrn, k/…/ calls,
 * and CASE-INSENSITIVE names (extrn print … <Print …>).
 */
public class Refal2DialectTest extends BasePlatformTestCase {

    public void testRefal2ContentIsDetectedAndLambdaIsNot() {
        myFixture.configureByText("hello.ref",
                "hello start\n"
                + " entry go\n"
                + "go = <Print 'Hello World!'>\n"
                + " end\n");
        assertEquals("Refal2", myFixture.getFile().getLanguage().getID());

        myFixture.configureByText("lambda.ref",
                "$ENTRY Go {\n  = <Prout 'x'>;\n}\n");
        assertEquals("Refal5Lambda", myFixture.getFile().getLanguage().getID());
    }

    public void testRefal2FileHighlightsWithoutErrors() {
        myFixture.configureByText("xcv.ref",
                "cv start\n"
                + "   entry CVB,cvd\n"
                + "   extrn NUMB,SYMB\n"
                + "CVB  '-' V(D)X = '-' K/cvb0/ VX.\n"
                + "cvb0 VX s1s2s3s4 = <NUMB s1s2s3s4>\n"
                + "     Ex = k/numb/Ex.\n"
                + " end\n");
        assertEquals("Refal2", myFixture.getFile().getLanguage().getID());
        for (HighlightInfo info : myFixture.doHighlighting()) {
            assertFalse("Unexpected error: " + info.getDescription(),
                    info.getSeverity() == HighlightSeverity.ERROR);
        }
    }

    public void testKCallResolvesCaseInsensitively() {
        myFixture.configureByText("m.ref",
                "m start\n"
                + " entry go\n"
                + "go = k/PRINT/ .\n"
                + "print = 'x'\n"
                + " end\n");
        PsiReference ref = myFixture.getFile().findReferenceAt(
                myFixture.getFile().getText().indexOf("PRINT"));
        assertNotNull(ref);
        Refal2Function resolved = assertInstanceOf(ref.resolve(), Refal2Function.class);
        assertEquals("print", resolved.getName());
    }

    public void testAngleCallResolvesCaseInsensitively() {
        myFixture.configureByText("m.ref",
                "m start\n"
                + " entry go\n"
                + "go = <Print>\n"
                + "print = 'x'\n"
                + " end\n");
        PsiReference ref = myFixture.getFile().findReferenceAt(
                myFixture.getFile().getText().indexOf("Print"));
        assertNotNull(ref);
        Refal2Function resolved = assertInstanceOf(ref.resolve(), Refal2Function.class);
        assertEquals("print", resolved.getName());
    }


    public void testDollarDirectiveVetoesRefal2Detection() {
        // A '$'-directive means Refal-5/5lambda; it must override any Refal-2-looking marker.
        myFixture.configureByText("mixed.ref",
                "$ENTRY Go { = ; }\n"
                + "entry go\n");           // bare 'entry' is an R2 marker, but '$ENTRY' vetoes it
        assertEquals("Refal5Lambda", myFixture.getFile().getLanguage().getID());
    }

    public void testPlainContentWithoutMarkersStaysLambda() {
        // No '$', no module header, no k/.../ call -> not Refal-2; default stays Refal-5lambda.
        myFixture.configureByText("plain.ref", "Double { e.X = e.X e.X; }\n");
        assertEquals("Refal5Lambda", myFixture.getFile().getLanguage().getID());
    }

    // --- Detection robustness: a Refal-2-looking token inside a string or comment must NOT
    // --- flip a Refal-5lambda file to Refal-2. (These failed before the content-aware rewrite.)

    public void testKSlashInsideStringDoesNotTriggerRefal2() {
        myFixture.configureByText("a.ref", "Helper { = <Prout 'mount k/usr to disk'>; }\n");
        assertEquals("Refal5Lambda", myFixture.getFile().getLanguage().getID());
    }

    public void testKSlashInsideCommentDoesNotTriggerRefal2() {
        myFixture.configureByText("a.ref", "* the k/v cache helper\n$ENTRY Go { = ; }\n");
        assertEquals("Refal5Lambda", myFixture.getFile().getLanguage().getID());
    }

    public void testBraceBlockIsAStrongLambdaSignal() {
        // No '$' and no markers, but a brace block -> Refal-5/5lambda (Refal-2 has no braces).
        myFixture.configureByText("a.ref", "Double { e.X = e.X e.X; }\n");
        assertEquals("Refal5Lambda", myFixture.getFile().getLanguage().getID());
    }

    // --- Explicit override: the deterministic escape hatch for ambiguous files.

    public void testOverrideCommentForcesRefal2() {
        // Content has no module header, so detection alone would say Refal-5lambda; the tag wins.
        myFixture.configureByText("frag.ref", "* refal-2\ncvb0 VX = <NUMB VX>\n");
        assertEquals("Refal2", myFixture.getFile().getLanguage().getID());
    }

    public void testOverrideCommentForcesLambda() {
        // Content looks like Refal-2 (name start / entry), but the tag forces Refal-5lambda.
        myFixture.configureByText("m.ref", "* refal-5\nfoo start\n entry go\n end\n");
        assertEquals("Refal5Lambda", myFixture.getFile().getLanguage().getID());
    }

    // --- Each dialect gets its own file icon, chosen by the detected language.

    public void testDialectFileIconsDifferByLanguage() {
        RefalDialectIconProvider provider = new RefalDialectIconProvider();
        myFixture.configureByText("r2.ref", "m start\n entry go\ngo = <Print>\n end\n");
        assertSame(RefalIcons.FILE_R2, provider.getIcon(myFixture.getFile(), 0));
        myFixture.configureByText("lam.ref", "$ENTRY Go { = ; }\n");
        assertSame(RefalIcons.FILE, provider.getIcon(myFixture.getFile(), 0));
    }
}
