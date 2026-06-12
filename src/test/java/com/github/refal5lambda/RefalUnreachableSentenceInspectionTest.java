package com.github.refal5lambda;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Catch-all detection: a lone e-variable pattern in a non-last sentence is flagged; the last
 * sentence, structured patterns, multi-variable patterns and where-clauses are not.
 */
public class RefalUnreachableSentenceInspectionTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(new RefalUnreachableSentenceInspection());
    }

    public void testCatchAllBeforeOtherSentencesIsFlagged() {
        myFixture.configureByText("a.ref",
                "F {\n"
                + "  <warning descr=\"Catch-all pattern: the sentences below it are unreachable\">e.X</warning> = ;\n"
                + "  'a' = ;\n"
                + "}\n");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testCatchAllAsLastSentenceIsFine() {
        myFixture.configureByText("a.ref",
                "F {\n"
                + "  'a' = ;\n"
                + "  e.X = ;\n"
                + "}\n");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testFallibleVariantsAreNotFlagged() {
        myFixture.configureByText("a.ref",
                "F {\n"
                + "  (e.X) = ;\n"
                + "  e.A e.B = ;\n"
                + "  e.C, e.C : 'a' = ;\n"
                + "  'z' = ;\n"
                + "}\n");
        myFixture.checkHighlighting(true, false, false);
    }
}
