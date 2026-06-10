package com.github.refal5lambda;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

/** Verifies that completion offers the variables in scope (and not those from other functions). */
public class RefalCompletionTest extends BasePlatformTestCase {

    public void testInScopeVariablesOffered() {
        myFixture.configureByText("a.ref", "Fact {\n  e.X s.Y = <Foo <caret>>;\n}\n");
        myFixture.complete(CompletionType.BASIC);
        List<String> items = myFixture.getLookupElementStrings();
        assertNotNull(items);
        assertTrue("offers e.X from the sentence", items.contains("e.X"));
        assertTrue("offers s.Y from the sentence", items.contains("s.Y"));
        assertTrue("still offers built-ins", items.contains("Prout"));
    }

    public void testVariablesDoNotLeakAcrossFunctions() {
        myFixture.configureByText("a.ref",
                "Foo {\n  e.X = e.X;\n}\nBar {\n  s.Z = <Mul s.Z <caret>>;\n}\n");
        myFixture.complete(CompletionType.BASIC);
        List<String> items = myFixture.getLookupElementStrings();
        assertNotNull(items);
        assertTrue("offers s.Z from the current sentence", items.contains("s.Z"));
        assertFalse("does not offer e.X from another function", items.contains("e.X"));
    }
}
