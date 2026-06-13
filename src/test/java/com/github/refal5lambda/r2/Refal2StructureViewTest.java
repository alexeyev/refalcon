package com.github.refal5lambda.r2;

import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.ArrayList;
import java.util.List;

/** The structure view lists Refal-2 functions. */
public class Refal2StructureViewTest extends BasePlatformTestCase {

    public void testRefal2FunctionsAppearInStructureView() {
        myFixture.configureByText("m.ref",
                "m start\n entry go\ngo = <Print>\nhelper = '1'\n end\n");
        assertEquals("Refal2", myFixture.getFile().getLanguage().getID());
        Refal2StructureViewModel model = new Refal2StructureViewModel(null, myFixture.getFile());
        try {
            List<String> names = new ArrayList<>();
            for (TreeElement child : model.getRoot().getChildren()) {
                names.add(child.getPresentation().getPresentableText());
            }
            assertContainsElements(names, "go", "helper");
        } finally {
            model.dispose();
        }
    }
}
