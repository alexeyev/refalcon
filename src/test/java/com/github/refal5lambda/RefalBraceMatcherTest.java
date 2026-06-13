package com.github.refal5lambda;

import com.github.refal5lambda.r2.Refal2BraceMatcher;
import com.github.refal5lambda.r2.Refal2TokenTypes;
import com.intellij.lang.BracePair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Bracket pairs: lambda matches {} () <> []; Refal-2 matches () <> and k/ ... . */
public class RefalBraceMatcherTest {

    private static boolean hasPair(BracePair[] pairs, com.intellij.psi.tree.IElementType open,
                                   com.intellij.psi.tree.IElementType close) {
        for (BracePair p : pairs) {
            if (p.getLeftBraceType() == open && p.getRightBraceType() == close) return true;
        }
        return false;
    }

    @Test
    public void lambdaPairs() {
        BracePair[] pairs = new RefalBraceMatcher().getPairs();
        assertEquals(4, pairs.length);
        assertTrue(hasPair(pairs, RefalTokenTypes.LBRACE, RefalTokenTypes.RBRACE));
        assertTrue(hasPair(pairs, RefalTokenTypes.LPAREN, RefalTokenTypes.RPAREN));
        assertTrue(hasPair(pairs, RefalTokenTypes.LANGLE, RefalTokenTypes.RANGLE));
    }

    @Test
    public void refal2Pairs() {
        BracePair[] pairs = new Refal2BraceMatcher().getPairs();
        assertEquals(3, pairs.length);
        assertTrue(hasPair(pairs, Refal2TokenTypes.LPAREN, Refal2TokenTypes.RPAREN));
        assertTrue(hasPair(pairs, Refal2TokenTypes.LANGLE, Refal2TokenTypes.RANGLE));
        // k/ ... . concretization brackets
        assertTrue(hasPair(pairs, Refal2TokenTypes.KOPEN, Refal2TokenTypes.DOT));
    }
}
