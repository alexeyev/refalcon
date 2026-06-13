package com.github.refal5lambda;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/** Multi-line function blocks produce fold regions. */
public class RefalFoldingTest extends BasePlatformTestCase {

    public void testFunctionBlockIsFoldable() {
        myFixture.configureByText("a.ref", "$ENTRY Go {\n  = <Prout 'hi'>;\n}\n");
        FoldingDescriptor[] regions = new RefalFoldingBuilder()
                .buildFoldRegions(myFixture.getFile(), myFixture.getEditor().getDocument(), false);
        assertTrue("expected at least one fold region for the block", regions.length >= 1);
    }
}
