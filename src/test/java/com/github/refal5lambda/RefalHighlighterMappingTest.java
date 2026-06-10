package com.github.refal5lambda;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Pins the highlighter color mappings, in particular that activation brackets {@code < >} do NOT
 * share a default color with variables (a real user report: with both based on plain text they
 * looked identical in the default themes).
 */
public class RefalHighlighterMappingTest extends BasePlatformTestCase {

    public void testAnglesAreMappedToTheirOwnKey() {
        RefalSyntaxHighlighter h = new RefalSyntaxHighlighter();
        assertOrderedEquals(h.getTokenHighlights(RefalTokenTypes.LANGLE), RefalSyntaxHighlighter.ANGLES);
        assertOrderedEquals(h.getTokenHighlights(RefalTokenTypes.RANGLE), RefalSyntaxHighlighter.ANGLES);
    }

    public void testAnglesDoNotShareDefaultsWithVariables() {
        assertSame(DefaultLanguageHighlighterColors.KEYWORD,
                RefalSyntaxHighlighter.ANGLES.getFallbackAttributeKey());
        assertNotSame(RefalSyntaxHighlighter.S_VARIABLE.getFallbackAttributeKey(),
                RefalSyntaxHighlighter.ANGLES.getFallbackAttributeKey());
    }
}
