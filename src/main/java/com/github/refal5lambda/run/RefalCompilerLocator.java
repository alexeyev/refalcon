package com.github.refal5lambda.run;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EnvironmentUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Best-effort discovery of the Refal-5 Lambda toolchain binaries ({@code rlc}, {@code rlmake}) so
 * the run configuration works without setup.
 *
 * <p>Windows facts (verified by inspecting the official {@code setup-refal-5-lambda-*.exe}
 * payload): the toolchain ships {@code bin\rlc.bat} and {@code bin\rlmake.bat} — there are
 * <b>no</b> {@code rlc.exe}/{@code rlmake.exe}; the engines are {@code *-core.exe} and the entry
 * points are batch wrappers — and it installs under {@code %APPDATA%\Refal-5-lambda}. So on
 * Windows we look for {@code <tool>.bat} first (then {@code .cmd}/{@code .exe} for custom
 * setups), and we search the {@code %APPDATA%} install location directly — which also rescues the
 * common case of an IDE started with a stale PATH (tool installed while the IDE — or JetBrains
 * Toolbox — was already running).
 *
 * <p>Detection does dozens of {@code stat()} calls (PATH entries × candidate names) and is
 * invoked on every external-annotation pass and during run-configuration validation (EDT), so
 * results are memoized per tool with a short TTL: a freshly installed tool is discoverable within
 * half a minute without re-scanning the file system on every keystroke.
 */
public final class RefalCompilerLocator {
    private RefalCompilerLocator() {}

    private static final long TTL_MS = 30_000;
    private static final ConcurrentHashMap<String, Snapshot> CACHE = new ConcurrentHashMap<>();

    private static final class Snapshot {
        final String path;          // null = not found
        final long at;
        Snapshot(String path, long at) { this.path = path; this.at = at; }
    }

    /** Order matters: {@code <tool>.bat} is what the official Windows distribution actually ships. */
    static String[] windowsCandidates(@NotNull String tool) {
        return new String[]{tool + ".bat", tool + ".cmd", tool + ".exe"};
    }

    /** @return absolute path to {@code rlc} if found on PATH or in a common location, else {@code null}. */
    public static @Nullable String detect() {
        return detectTool("rlc");
    }

    /** Same as {@link #detect()} for an arbitrary toolchain binary, e.g. {@code "rlmake"}. */
    public static @Nullable String detectTool(@NotNull String tool) {
        Snapshot s = CACHE.get(tool);
        long now = System.currentTimeMillis();
        if (s != null && now - s.at < TTL_MS) return s.path;
        String found = doDetect(tool);
        CACHE.put(tool, new Snapshot(found, now));   // benign race: doDetect is idempotent
        return found;
    }

    private static @Nullable String doDetect(String tool) {
        // EnvironmentUtil captures the login-shell PATH, so detection also works when the IDE was
        // launched from the macOS Dock / a desktop launcher (where System.getenv("PATH") is often
        // incomplete). Fall back to the process PATH if the captured one is unavailable.
        String path = EnvironmentUtil.getValue("PATH");
        if (path == null) path = System.getenv("PATH");
        if (path != null) {
            for (String dir : StringUtil.split(path, File.pathSeparator)) {
                String hit = executableIn(dir, tool);
                if (hit != null) return hit;
            }
        }
        for (String dir : commonDirs()) {
            String hit = executableIn(dir, tool);
            if (hit != null) return hit;
        }
        return null;
    }

    private static @Nullable String executableIn(String dir, String tool) {
        if (StringUtil.isEmptyOrSpaces(dir)) return null;
        String[] names = SystemInfo.isWindows ? windowsCandidates(tool) : new String[]{tool};
        for (String name : names) {
            File f = new File(dir, name);
            // canExecute() is meaningless for .bat files, so on Windows existence is enough.
            boolean ok = SystemInfo.isWindows ? f.isFile() : f.isFile() && f.canExecute();
            if (ok) return f.getAbsolutePath();
        }
        return null;
    }

    /** Visible for tests. Unix entries include the documented "clone into home" locations. */
    static String[] commonDirs() {
        String home = System.getProperty("user.home", "");
        if (SystemInfo.isWindows) {
            List<String> dirs = new ArrayList<>();
            String appData = System.getenv("APPDATA");
            if (StringUtil.isEmptyOrSpaces(appData)) appData = home + "\\AppData\\Roaming";
            dirs.add(appData + "\\Refal-5-lambda\\bin");   // official installer layout (bin\<tool>.bat)
            dirs.add(appData + "\\Refal-5-lambda");
            dirs.add(home + "\\.local\\bin");
            dirs.add(home + "\\bin");
            return dirs.toArray(new String[0]);
        }
        return new String[]{
                home + "/.local/bin",
                home + "/bin",
                // The official install docs say: clone the repo, run ./bootstrap.sh, add bin/ to
                // PATH. These cover users who cloned into home but skipped the PATH step.
                home + "/refal-5-lambda/bin",
                home + "/simple-refal-distrib/bin",
                "/usr/local/bin",
                "/opt/homebrew/bin",
                "/opt/refal-5-lambda/bin",
                "/usr/bin",
        };
    }
}
