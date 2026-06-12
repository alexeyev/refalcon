package com.github.refal5lambda.r2;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiReference;
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

}
