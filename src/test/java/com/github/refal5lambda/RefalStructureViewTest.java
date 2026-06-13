package com.github.refal5lambda;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.ArrayList;
import java.util.List;

/** The file structure view lists the Refal-5lambda functions in the file. */
public class RefalStructureViewTest extends BasePlatformTestCase {

    public void testFunctionsAppearInStructureView() {
        myFixture.configureByText("a.ref", "$ENTRY Go { = <Helper>; }\nHelper { = ; }\n");
        RefalStructureViewModel model = new RefalStructureViewModel(myFixture.getFile());
        try {
            List<String> names = new ArrayList<>();
            for (TreeElement child : model.getRoot().getChildren()) {
                names.add(child.getPresentation().getPresentableText());
            }
            assertContainsElements(names, "Go", "Helper");
        } finally {
            model.dispose();
        }
    }
}
