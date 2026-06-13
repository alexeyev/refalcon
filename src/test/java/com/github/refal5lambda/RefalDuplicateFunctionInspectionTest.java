package com.github.refal5lambda;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/** Duplicate-definition detection: every clashing definition is flagged; unique names are not. */
public class RefalDuplicateFunctionInspectionTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(new RefalDuplicateFunctionInspection());
    }

    public void testBothDuplicateDefinitionsAreFlagged() {
        myFixture.configureByText("a.ref",
                "<warning descr=\"Function 'F' is defined 2 times in this file\">F</warning> { = '1'; }\n"
                + "Unique { = ; }\n"
                + "<warning descr=\"Function 'F' is defined 2 times in this file\">F</warning> { = '2'; }\n");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testUniqueAndRecursiveNamesAreNotFlagged() {
        myFixture.configureByText("a.ref",
                "$ENTRY Go { = <Loop>; }\n"
                + "Loop { = <Loop>; }\n");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testDifferentCaseNamesAreNotDuplicates() {
        // Refal-5lambda names are case-sensitive, so 'F' and 'f' are distinct functions.
        myFixture.configureByText("a.ref",
                "F { = '1'; }\n"
                + "f { = '2'; }\n");
        myFixture.checkHighlighting(true, false, false);
    }
}
