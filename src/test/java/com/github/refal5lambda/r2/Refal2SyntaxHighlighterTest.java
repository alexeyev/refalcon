package com.github.refal5lambda.r2;

import com.github.refal5lambda.RefalSyntaxHighlighter;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/** Refal-2 reuses the shared color keys: keywords, names, calls, variables, activation brackets. */
public class Refal2SyntaxHighlighterTest extends BasePlatformTestCase {

    public void testTokenColorMapping() {
        Refal2SyntaxHighlighter h = new Refal2SyntaxHighlighter();
        assertOrderedEquals(h.getTokenHighlights(Refal2TokenTypes.KEYWORD), RefalSyntaxHighlighter.KEYWORD);
        assertOrderedEquals(h.getTokenHighlights(Refal2TokenTypes.DEF_NAME), RefalSyntaxHighlighter.FUNCTION_DEFINITION);
        assertOrderedEquals(h.getTokenHighlights(Refal2TokenTypes.FUNC), RefalSyntaxHighlighter.FUNCTION_CALL);
        // Both the angle call and the k/.../ concretization brackets use the activation-bracket color.
        assertOrderedEquals(h.getTokenHighlights(Refal2TokenTypes.LANGLE), RefalSyntaxHighlighter.ANGLES);
        assertOrderedEquals(h.getTokenHighlights(Refal2TokenTypes.KOPEN), RefalSyntaxHighlighter.ANGLES);
    }
}
