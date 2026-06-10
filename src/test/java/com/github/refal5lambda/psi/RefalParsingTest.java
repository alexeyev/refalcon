package com.github.refal5lambda.psi;

import com.intellij.testFramework.ParsingTestCase;

/**
 * Validates the PSI tree produced by {@link RefalParserDefinition} / {@link RefalParser}.
 * The expected tree lives in src/test/testData/Hello.txt (generated on first run).
 */
public class RefalParsingTest extends ParsingTestCase {
    public RefalParsingTest() {
        super("", "ref", new RefalParserDefinition());
    }

    public void testHello() {
        doTest(true);
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }
}
