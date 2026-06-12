package com.github.refal5lambda.run;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Guards the Windows compiler-detection facts (verified by inspecting the official installer
 * payload, setup-refal-5-lambda-3.3.1.exe): the distribution ships {@code bin\rlc.bat} and NO
 * {@code rlc.exe}, so detection must consider {@code rlc.bat} — and prefer it.
 */
public class RefalCompilerResolutionTest {

    @Test
    public void windowsDetectionPrefersTheBatWrapperTheToolchainActuallyShips() {
        assertEquals("rlc.bat", RefalCompilerLocator.windowsCandidates("rlc")[0]);
        assertEquals("rlmake.bat", RefalCompilerLocator.windowsCandidates("rlmake")[0]);
        assertTrue(Arrays.asList(RefalCompilerLocator.windowsCandidates("rlc")).contains("rlc.exe"));
    }

    @Test
    public void unixSetupDirsCoverDocumentedCloneLocations() {
        // The official install flow is "clone into home, run bootstrap.sh, add bin/ to PATH";
        // detection must work even when the user skipped the PATH step.
        String home = System.getProperty("user.home", "");
        java.util.List<String> dirs = java.util.Arrays.asList(RefalCompilerLocator.commonDirs());
        assertTrue(dirs.contains(home + "/refal-5-lambda/bin"));
        assertTrue(dirs.contains(home + "/simple-refal-distrib/bin"));
        assertTrue(dirs.contains("/usr/local/bin"));
    }

    @Test
    public void explicitCompilerValuesPassThroughUntouched() {
        assertEquals("C:/tools/rlc.bat", RefalCommandLineState.resolveCompiler("C:/tools/rlc.bat", false));
        assertEquals("C:/tools/rlmake.bat", RefalCommandLineState.resolveCompiler("C:/tools/rlmake.bat", true));
    }
}
