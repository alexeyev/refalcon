package com.github.refal5lambda;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * The live-template context guards that Refal templates ({@code entry}, {@code fn}, {@code call},
 * ...) expand only inside Refal files, not in unrelated file types.
 */
public class RefalTemplateContextTypeTest extends BasePlatformTestCase {

    public void testActiveInRefalFiles() {
        myFixture.configureByText("a.ref", "$ENTRY Go { = ; }\n");
        TemplateActionContext ctx = TemplateActionContext.expanding(myFixture.getFile(), 0);
        assertTrue(new RefalTemplateContextType().isInContext(ctx));
    }

    public void testInactiveInOtherFiles() {
        myFixture.configureByText("a.txt", "just plain text\n");
        TemplateActionContext ctx = TemplateActionContext.expanding(myFixture.getFile(), 0);
        assertFalse(new RefalTemplateContextType().isInContext(ctx));
    }
}
