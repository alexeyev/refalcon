package com.github.refal5lambda.run;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for the pure command-line assembly in {@link RefalCommandLineState}: quoting of paths
 * with spaces, no double spaces when optional parts are empty (a cosmetic bug observed in real
 * Windows runs), and the ./ prefix for bare executables on Unix.
 */
public class RefalShellCommandTest {

    @Test
    public void emptyOptionsProduceNoDoubleSpace() {
        String line = RefalCommandLineState.buildShellCommand(
                "rlc", "", "C:/Users/me/hello.ref", "hello.exe", "", true);
        assertEquals("rlc C:/Users/me/hello.ref && hello.exe", line);
        assertFalse(line.contains("  "));
    }

    @Test
    public void pathsWithSpacesAreQuoted() {
        String line = RefalCommandLineState.buildShellCommand(
                "C:/Program Files/refal/rlc.exe", "", "C:/My Projects/h.ref", "h.exe", "", true);
        assertEquals("\"C:/Program Files/refal/rlc.exe\" \"C:/My Projects/h.ref\" && h.exe", line);
    }

    @Test
    public void optionsAndProgramArgsKeepTheirPlaces() {
        String line = RefalCommandLineState.buildShellCommand(
                "rlc", "-OP", "h.ref", "./h", "--verbose 1", false);
        assertEquals("rlc -OP h.ref && ./h --verbose 1", line);
    }

    @Test
    public void bareExecutableGetsDotSlashOnUnixOnly() {
        assertEquals("./h", RefalCommandLineState.shellExecutable("h", false));
        assertEquals("bin/h", RefalCommandLineState.shellExecutable("bin/h", false));   // already a path
        assertEquals("./h", RefalCommandLineState.shellExecutable("./h", false));       // unchanged
        assertEquals("h.exe", RefalCommandLineState.shellExecutable("h.exe", true));    // cmd uses cwd
    }
}
