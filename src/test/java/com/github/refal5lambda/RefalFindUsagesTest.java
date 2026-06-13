package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalFunction;
import com.github.refal5lambda.r2.Refal2Function;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.usageView.UsageInfo;

import java.util.Collection;

/** Find Usages locates calls to a function definition, in both dialects. */
public class RefalFindUsagesTest extends BasePlatformTestCase {

    public void testLambdaFindUsages() {
        myFixture.configureByText("a.ref", "$ENTRY Go { = <Helper>; }\nHelper { = ; }\n");
        RefalFunction helper = named(RefalFunction.class, "Helper");
        Collection<UsageInfo> usages = myFixture.findUsages(helper);
        assertEquals(1, usages.size());
    }

    public void testRefal2FindUsages() {
        // Case-matching call so the (case-sensitive) word index finds it.
        myFixture.configureByText("m.ref", "m start\n entry go\ngo = <print>\nprint = 'x'\n end\n");
        assertEquals("Refal2", myFixture.getFile().getLanguage().getID());
        Refal2Function print = named(Refal2Function.class, "print");
        Collection<UsageInfo> usages = myFixture.findUsages(print);
        assertTrue("expected at least one usage of print", usages.size() >= 1);
    }

    private <T extends com.intellij.psi.PsiNamedElement> T named(Class<T> cls, String name) {
        for (T e : PsiTreeUtil.findChildrenOfType(myFixture.getFile(), cls)) {
            if (name.equals(e.getName())) return e;
        }
        throw new AssertionError("no " + cls.getSimpleName() + " named " + name);
    }
}
