package com.github.refal5lambda;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * The indented-asterisk inspection: a '*' starts a comment only at column 0 in every Refal
 * dialect; indented it is code. Column-0 comments are never flagged; the quick-fix moves the
 * line back to column 0.
 */
public class RefalIndentedStarCommentTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(new RefalIndentedStarCommentInspection());
    }

    public void testIndentedStarIsFlaggedButColumnZeroIsNot() {
        myFixture.configureByText("a.ref",
                "* fine\n"
                + "$ENTRY Go {\n"
                + "  = ;\n"
                + "  <warning descr=\"An asterisk starts a comment only at column 0 \u2014 indented, this line is code\">*</warning> oops\n"
                + "}\n");
        myFixture.checkHighlighting(true, false, false);
    }

    public void testQuickFixMovesLineToColumnZero() {
        myFixture.configureByText("a.ref",
                "Go {\n"
                + "  = ;\n"
                + "   <caret>* note\n"
                + "}\n");
        IntentionAction fix = myFixture.findSingleIntention("Move comment to column 0");
        myFixture.launchAction(fix);
        myFixture.checkResult(
                "Go {\n"
                + "  = ;\n"
                + "* note\n"
                + "}\n");
    }
}
