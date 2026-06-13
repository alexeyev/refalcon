package com.github.refal5lambda;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/** Rename relies on this to validate new names. */
public class RefalNamesValidatorTest extends BasePlatformTestCase {

    public void testIdentifierValidation() {
        RefalNamesValidator v = new RefalNamesValidator();
        assertTrue(v.isIdentifier("Palindrome", getProject()));
        assertFalse(v.isIdentifier("has space", getProject()));
        assertFalse(v.isIdentifier("", getProject()));
    }
}
