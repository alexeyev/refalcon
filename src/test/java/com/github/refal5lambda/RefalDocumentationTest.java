package com.github.refal5lambda;

import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/** Verifies Ctrl+Q documentation for built-ins and for in-file functions. */
public class RefalDocumentationTest extends BasePlatformTestCase {

    public void testBuiltinDoc() {
        myFixture.configureByText("a.ref", "Go {\n  = <Pro<caret>ut 'hi'>;\n}\n");
        PsiElement leaf = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertNotNull(leaf);
        String doc = new RefalDocumentationProvider().generateDoc(leaf, leaf);
        assertNotNull("a built-in should have documentation", doc);
        assertTrue(doc.contains("Prout"));
        assertTrue(doc.contains("built-in"));
    }

    public void testUserFunctionDoc() {
        myFixture.configureByText("a.ref", "Go {\n  = <Fa<caret>ct 5>;\n}\nFact {\n  0 = 1;\n}\n");
        PsiElement leaf = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertNotNull(leaf);
        String doc = new RefalDocumentationProvider().generateDoc(leaf, leaf);
        assertNotNull("an in-file function should have documentation", doc);
        assertTrue(doc.contains("Fact"));
        assertTrue(doc.contains("function"));
    }
}
