package com.github.refal5lambda;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link RefalAnnotator} (instant, compiler-free highlighting). As well as checking that
 * real problems are flagged, several tests guard against false positives on valid code (conditions,
 * closures, result-side matches, library calls) — the hard part of doing this without the compiler.
 */
public class RefalAnnotatorTest extends BasePlatformTestCase {

    private List<String> errors(String text) {
        myFixture.configureByText("a.ref", text);
        List<String> descriptions = new ArrayList<>();
        for (HighlightInfo info : myFixture.doHighlighting(HighlightSeverity.ERROR)) {
            if (info.getDescription() != null) descriptions.add(info.getDescription());
        }
        return descriptions;
    }

    private static boolean any(List<String> xs, String needle) {
        return xs.stream().anyMatch(s -> s.contains(needle));
    }

    // --- unresolved function ---

    public void testUnresolvedFunctionFlagged() {
        assertTrue(any(errors("$ENTRY Go {\n  = <Nonexistent>;\n}\n"), "Unresolved function"));
    }

    public void testBuiltinNotFlagged() {
        assertFalse(any(errors("$ENTRY Go {\n  = <Prout 'hi'>;\n}\n"), "Unresolved function"));
    }

    public void testStandardLibraryFunctionNotFlagged() {
        // Map is a standard-library $ENTRY; must not be flagged
        assertFalse(any(errors("$ENTRY Go {\n  = <Map Prout ('a')('b')>;\n}\n"), "Unresolved function"));
    }

    public void testDefinedFunctionNotFlagged() {
        assertFalse(any(errors("$ENTRY Go {\n  = <Helper>;\n}\nHelper {\n  = ;\n}\n"), "Unresolved"));
    }

    public void testExternDeclaredFunctionNotFlagged() {
        assertFalse(any(errors("$EXTERN Ext;\n$ENTRY Go {\n  = <Ext>;\n}\n"), "Unresolved function"));
    }

    // --- unresolved variable ---

    public void testUnresolvedVariableFlagged() {
        assertTrue(any(errors("$ENTRY Go {\n  e.X = e.Y;\n}\n"), "Unresolved variable"));
    }

    public void testBoundVariableNotFlagged() {
        assertFalse(any(errors("$ENTRY Go {\n  e.X = e.X;\n}\n"), "Unresolved variable"));
    }

    public void testConditionBoundVariableNotFlagged() {
        // a classic condition binds e.New (it is left of the final '='): no false positive
        assertFalse(any(errors("F {\n  e.X , <Lenw e.X> : s.N e.New = e.New;\n}\n"), "Unresolved variable"));
    }

    public void testNestedBlockClosureNotFlagged() {
        // inner sentence uses e.X from the outer pattern (closure): no false positive
        assertFalse(any(errors("F {\n  e.X = <Foo e.X> {\n    s.Y = e.X s.Y;\n  };\n}\n"), "Unresolved variable"));
    }
}
