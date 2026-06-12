package com.github.refal5lambda;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Reformat Code for Refal. The third test pins the language-specific safety rule: a {@code *}
 * comment is a comment ONLY at column 0, so the formatter must never indent it (doing so would
 * silently turn the comment into code).
 */
public class RefalFormatterTest extends BasePlatformTestCase {

    private void reformat() {
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            CodeStyleManager.getInstance(getProject()).reformat(myFixture.getFile());
        });
    }

    public void testSpacingNormalizationOnOneLine() {
        myFixture.configureByText("a.ref", "F{e.X=e.X;}");
        reformat();
        myFixture.checkResult("F { e.X = e.X; }");
    }

    public void testIndentationOfSentencesAndContinuations() {
        myFixture.configureByText("a.ref", "F {\ne.X\n= e.X;\n}\n");
        reformat();
        myFixture.checkResult("F {\n  e.X\n    = e.X;\n}\n");
    }

    public void testLineCommentStaysAtColumnZero() {
        String pinned = "F {\n  e.X = e.X;\n* still a comment, must not be indented\n}\n";
        myFixture.configureByText("a.ref", pinned);
        reformat();
        myFixture.checkResult(pinned);
    }

    public void testCanonicalConditionStyleSurvives() {
        String canonical = "F {\n  e.X\n    , <Check e.X> : True\n    = <Process e.X>;\n}\n";
        myFixture.configureByText("a.ref", canonical);
        reformat();
        myFixture.checkResult(canonical);
    }
}
