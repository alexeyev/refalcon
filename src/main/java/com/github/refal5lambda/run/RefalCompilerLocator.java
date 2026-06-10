package com.github.refal5lambda.run;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EnvironmentUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/** Best-effort discovery of the Refal-5 Lambda compiler (rlc) so the run config works without setup. */
public final class RefalCompilerLocator {
    private RefalCompilerLocator() {}

    private static final String EXE = SystemInfo.isWindows ? "rlc.exe" : "rlc";

    /** @return absolute path to rlc if found on PATH or in a common location, otherwise {@code null}. */
    public static @Nullable String detect() {
        // EnvironmentUtil captures the login-shell PATH, so detection also works when the IDE was
        // launched from the macOS Dock / a desktop launcher (where System.getenv("PATH") is often
        // incomplete). Fall back to the process PATH if the captured one is unavailable.
        String path = EnvironmentUtil.getValue("PATH");
        if (path == null) path = System.getenv("PATH");
        if (path != null) {
            for (String dir : StringUtil.split(path, File.pathSeparator)) {
                String hit = executableIn(dir);
                if (hit != null) return hit;
            }
        }
        for (String dir : commonDirs()) {
            String hit = executableIn(dir);
            if (hit != null) return hit;
        }
        return null;
    }

    private static @Nullable String executableIn(String dir) {
        if (StringUtil.isEmptyOrSpaces(dir)) return null;
        File f = new File(dir, EXE);
        return f.isFile() && f.canExecute() ? f.getAbsolutePath() : null;
    }

    private static String[] commonDirs() {
        String home = System.getProperty("user.home", "");
        if (SystemInfo.isWindows) {
            return new String[]{home + "\\.local\\bin", home + "\\bin"};
        }
        return new String[]{
                home + "/.local/bin",
                home + "/bin",
                "/usr/local/bin",
                "/opt/homebrew/bin",
                "/opt/refal-5-lambda/bin",
                "/usr/bin",
        };
    }
}
