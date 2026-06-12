package com.github.refal5lambda;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Quick-fixes attached to the "Unresolved function" annotation: create a stub definition, or
 * declare the name via {@code $EXTERN}. The third test pins the anchor rule — a new
 * {@code $EXTERN} goes after declaration directives but must never be inserted between
 * {@code $ENTRY} and its function.
 */
public class RefalQuickFixTest extends BasePlatformTestCase {

    public void testCreateFunctionFromUnresolvedCall() {
        myFixture.configureByText("a.ref", "$ENTRY Go { = <Hel<caret>per>; }\n");
        IntentionAction fix = myFixture.findSingleIntention("Create function 'Helper'");
        myFixture.launchAction(fix);
        myFixture.checkResult("$ENTRY Go { = <Helper>; }\n\nHelper {\n  = <caret>;\n}\n");
    }

    public void testAddExternWithNoDeclarationDirectives() {
        myFixture.configureByText("a.ref", "$ENTRY Go { = <Hel<caret>per>; }\n");
        IntentionAction fix = myFixture.findSingleIntention("Add '$EXTERN Helper;'");
        myFixture.launchAction(fix);
        myFixture.checkResult("$EXTERN Helper;\n$ENTRY Go { = <Helper>; }\n");
    }

    public void testAddExternGoesAfterExistingDeclarations() {
        myFixture.configureByText("a.ref", "$EXTERN Other;\n$ENTRY Go { = <Hel<caret>per>; }\n");
        IntentionAction fix = myFixture.findSingleIntention("Add '$EXTERN Helper;'");
        myFixture.launchAction(fix);
        myFixture.checkResult("$EXTERN Other;\n$EXTERN Helper;\n$ENTRY Go { = <Helper>; }\n");
    }
}
