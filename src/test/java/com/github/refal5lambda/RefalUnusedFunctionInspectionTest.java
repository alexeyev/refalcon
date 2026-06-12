package com.github.refal5lambda;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * The Unused-function inspection: reports dead functions, but never {@code $ENTRY} functions,
 * never {@code Go}/{@code GO}, and never functions that are used from another file.
 */
public class RefalUnusedFunctionInspectionTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(new RefalUnusedFunctionInspection());
    }

    public void testUnusedFunctionIsReportedButEntryAndGoAreNot() {
        myFixture.configureByText("a.ref",
                "Used { = ; }\n"
                + "$ENTRY Exported { = ; }\n"
                + "$ENTRY Go { = <Used>; }\n"
                + "<warning descr=\"Function 'Dead' is never used\">Dead</warning> { = ; }\n");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testRecursiveOnlyFunctionCountsAsUsed() {
        myFixture.configureByText("a.ref",
                "$ENTRY Go { = <Loop>; }\n"
                + "Loop { = <Loop>; }\n");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testFunctionUsedFromAnotherFileIsNotReported() {
        myFixture.addFileToProject("main.ref",
                "$EXTERN Helper;\n$ENTRY Go { = <Helper>; }\n");
        myFixture.configureByText("lib.ref", "Helper { = <Prout 'x'>; }\n");
        myFixture.checkHighlighting(true, false, false);
    }
}
