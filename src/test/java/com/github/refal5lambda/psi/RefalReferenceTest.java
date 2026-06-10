package com.github.refal5lambda.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/** Exercises call -> definition resolution (Go to Declaration) and Rename. */
public class RefalReferenceTest extends BasePlatformTestCase {

    public void testCallResolvesToDefinition() {
        myFixture.configureByText("a.ref",
                "$ENTRY Go {\n  = <Fa<caret>ct 5>;\n}\nFact {\n  0 = 1;\n}\n");
        PsiElement target = myFixture.getElementAtCaret();
        assertTrue("call should resolve to a function definition", target instanceof RefalFunction);
        assertEquals("Fact", ((RefalFunction) target).getName());
    }

    public void testBuiltinCallDoesNotResolve() {
        myFixture.configureByText("a.ref", "Go {\n  = <Pro<caret>ut 'hi'>;\n}\n");
        PsiReference ref = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
        assertNotNull("a reference is attached to the call", ref);
        assertNull("a call to an undefined (library) function resolves to nothing", ref.resolve());
    }

    public void testCrossFileResolution() {
        myFixture.addFileToProject("lib.ref",
                "Fact {\n  0 = 1;\n  s.N = <Mul s.N <Fact <Sub s.N 1>>>;\n}\n");
        myFixture.configureByText("main.ref",
                "$EXTERN Fact;\n$ENTRY Go {\n  = <Fa<caret>ct 5>;\n}\n");
        PsiElement target = myFixture.getElementAtCaret();
        assertTrue("an $EXTERN call should resolve to a definition", target instanceof RefalFunction);
        assertEquals("Fact", ((RefalFunction) target).getName());
        assertEquals("definition lives in the other file", "lib.ref", target.getContainingFile().getName());
    }

    public void testRenameDefinitionUpdatesCall() {
        myFixture.configureByText("a.ref",
                "$ENTRY Go {\n  = <Fact 5>;\n}\nFa<caret>ct {\n  0 = 1;\n}\n");
        myFixture.renameElementAtCaret("Factorial");
        String text = myFixture.getFile().getText();
        assertTrue("definition renamed", text.contains("Factorial {"));
        assertTrue("call renamed", text.contains("<Factorial 5>"));
    }
}
