package com.github.refal5lambda;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Line commenting uses a column-0 '*' in both dialects. Comment and uncomment are tested from
 * fixed caret positions (the comment action advances the caret, so a single toggle is verified
 * per direction rather than a round-trip).
 */
public class RefalCommenterTest extends BasePlatformTestCase {

    public void testLambdaComment() {
        myFixture.configureByText("a.ref", "<caret>Go { = ; }\n");
        myFixture.performEditorAction(IdeActions.ACTION_COMMENT_LINE);
        assertEquals("*Go { = ; }\n", myFixture.getEditor().getDocument().getText());
    }

    public void testLambdaUncomment() {
        myFixture.configureByText("b.ref", "<caret>*Go { = ; }\n");
        myFixture.performEditorAction(IdeActions.ACTION_COMMENT_LINE);
        assertEquals("Go { = ; }\n", myFixture.getEditor().getDocument().getText());
    }

    public void testRefal2Comment() {
        myFixture.configureByText("m.ref", "m start\n<caret>go = <Print>\n end\n");
        assertEquals("Refal2", myFixture.getFile().getLanguage().getID());
        myFixture.performEditorAction(IdeActions.ACTION_COMMENT_LINE);
        assertEquals("m start\n*go = <Print>\n end\n",
                myFixture.getEditor().getDocument().getText());
    }

    public void testRefal2Uncomment() {
        myFixture.configureByText("n.ref", "m start\n<caret>*go = <Print>\n end\n");
        assertEquals("Refal2", myFixture.getFile().getLanguage().getID());
        myFixture.performEditorAction(IdeActions.ACTION_COMMENT_LINE);
        assertEquals("m start\ngo = <Print>\n end\n",
                myFixture.getEditor().getDocument().getText());
    }
}
