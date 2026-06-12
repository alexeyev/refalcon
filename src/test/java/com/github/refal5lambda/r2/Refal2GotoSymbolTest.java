package com.github.refal5lambda.r2;

import com.intellij.navigation.NavigationItem;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.Arrays;

/** Navigate → Symbol must see Refal-2 functions in project files. */
public class Refal2GotoSymbolTest extends BasePlatformTestCase {

    public void testRefal2FunctionsAreListedAndNavigable() {
        myFixture.addFileToProject("m.ref",
                "m start\n"
                + " entry go\n"
                + "go = <Print>\n"
                + "print = 'x'\n"
                + " end\n");
        Refal2GotoSymbolContributor contributor = new Refal2GotoSymbolContributor();

        String[] names = contributor.getNames(getProject(), false);
        assertTrue(Arrays.asList(names).contains("go"));
        assertTrue(Arrays.asList(names).contains("print"));

        NavigationItem[] items = contributor.getItemsByName("print", "print", getProject(), false);
        assertEquals(1, items.length);
        assertTrue(items[0] instanceof Refal2Function);
        assertEquals("print", ((Refal2Function) items[0]).getName());
    }
}
