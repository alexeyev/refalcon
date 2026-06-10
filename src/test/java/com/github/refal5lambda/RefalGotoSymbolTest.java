package com.github.refal5lambda;

import com.intellij.navigation.NavigationItem;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.Arrays;
import java.util.List;

/**
 * "Go to Symbol" (Ctrl+Alt+Shift+N) must list every function defined anywhere in the project and
 * navigate to it — the key navigation feature for multi-file projects.
 */
public class RefalGotoSymbolTest extends BasePlatformTestCase {

    public void testListsFunctionsFromAllProjectFiles() {
        myFixture.addFileToProject("a.ref", "$ENTRY Go { = <Helper>; }\nHelper { = ; }\n");
        myFixture.addFileToProject("sub/b.ref", "$ENTRY OtherTool { = ; }\n");

        RefalGotoSymbolContributor contributor = new RefalGotoSymbolContributor();
        List<String> names = Arrays.asList(contributor.getNames(getProject(), false));

        assertTrue(names.contains("Go"));
        assertTrue(names.contains("Helper"));
        assertTrue(names.contains("OtherTool"));   // from the other file, in a subdirectory
    }

    public void testItemsNavigateToTheDefinition() {
        myFixture.addFileToProject("lib.ref", "Twice { e.X = e.X e.X; }\n");

        RefalGotoSymbolContributor contributor = new RefalGotoSymbolContributor();
        NavigationItem[] items = contributor.getItemsByName("Twice", "Twi", getProject(), false);

        assertEquals(1, items.length);
        assertEquals("Twice", items[0].getName());
        assertTrue(items[0].canNavigate());
    }
}
