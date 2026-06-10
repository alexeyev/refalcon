package com.github.refal5lambda.run;

import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link RefalConsoleFilter#LOCATION}. The relative-path samples mirror REAL rlc
 * output (e.g. {@code bad.ref:2:27: ERROR: Missed ')'}); the absolute Windows form is what rlc
 * echoes when the IDE passes a full path — the original pattern could not match it because the
 * drive colon fell into the excluded character class.
 */
public class RefalConsoleFilterPatternTest {

    @Test
    public void matchesRelativePathWithLineAndColumn() {
        Matcher m = RefalConsoleFilter.LOCATION.matcher("bad.ref:2:27: ERROR: Missed ')'");
        assertTrue(m.find());
        assertEquals("bad.ref", m.group(1));
        assertEquals("2", m.group(2));
        assertEquals("27", m.group(3));
    }

    @Test
    public void matchesAbsoluteWindowsPathWithDriveLetter() {
        Matcher m = RefalConsoleFilter.LOCATION.matcher(
                "C:/Users/ezdet/project/hello.ref:3:7: ERROR: Missed '>'");
        assertTrue(m.find());
        assertEquals("C:/Users/ezdet/project/hello.ref", m.group(1));
        assertEquals("3", m.group(2));
        assertEquals("7", m.group(3));
    }

    @Test
    public void matchesBackslashWindowsPath() {
        Matcher m = RefalConsoleFilter.LOCATION.matcher(
                "C:\\proj\\hello.ref:12:1: WARNING: something");
        assertTrue(m.find());
        assertEquals("C:\\proj\\hello.ref", m.group(1));
        assertEquals("12", m.group(2));
    }

    @Test
    public void ignoresLinesWithoutLineNumber() {
        // rlc prints progress lines like "*Compiling hello.ref:" — they must not become links.
        assertFalse(RefalConsoleFilter.LOCATION.matcher("*Compiling hello.ref:").find());
    }
}
