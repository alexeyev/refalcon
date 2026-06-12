package com.github.refal5lambda.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Builds the command line: compile the .ref with the configured compiler (rlc), and — if requested —
 * run the resulting executable. Compile-and-run is chained through the OS shell so a single Run does both.
 *
 * <p>The compiler is resolved before launch: an explicit path from the run configuration is used as
 * given; a blank field (or the legacy default {@code "rlc"}) triggers auto-detection on PATH and in
 * standard install locations. If nothing is found, the run fails fast with installation
 * instructions instead of the OS's cryptic {@code 'rlc' is not recognized…} message.
 */
final class RefalCommandLineState extends CommandLineState {

    /** Appended to the console when the shell itself reports "command not found" (9009 cmd / 127 sh). */
    static String commandNotFoundHint(int exitCode) {
        return "\n[Refalcon] The shell could not find a command (exit code " + exitCode + ").\n"
                + "If the missing command is the Refal compiler (rlc):\n"
                + "  \u2022 Windows: install setup-refal-5-lambda-<version>.exe from\n"
                + "    https://github.com/bmstu-iu9/refal-5-lambda/releases/latest\n"
                + "    (it puts rlc.bat into %APPDATA%\\Refal-5-lambda\\bin and adds it to the user PATH;\n"
                + "    no separate C++ compiler is needed \u2014 prebuilt runtime prefixes are included),\n"
                + "    then RESTART the IDE \u2014 and JetBrains Toolbox too if you launch through it,\n"
                + "    since IDEs inherit Toolbox's (possibly stale) PATH.\n"
                + "  \u2022 Linux/macOS: git clone https://github.com/bmstu-iu9/refal-5-lambda,\n"
                + "    run ./bootstrap.sh inside it (needs a C++ compiler; macOS: xcode-select --install),\n"
                + "    and add its bin/ directory to PATH.\n"
                + "Or set the full path to rlc in the Run Configuration field \"Refal compiler (rlc)\".\n"
                + "(The plugin also auto-detects rlc in %APPDATA%\\Refal-5-lambda even when PATH is stale.)\n";
    }

    private final RefalRunConfiguration config;

    RefalCommandLineState(@NotNull ExecutionEnvironment environment, @NotNull RefalRunConfiguration config) {
        super(environment);
        this.config = config;

        // Make compiler diagnostics (file:line:col) clickable in the Run console.
        TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(environment.getProject());
        builder.addFilter(new RefalConsoleFilter(environment.getProject(), workingDir(config)));
        setConsoleBuilder(builder);
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        String compiler = resolveCompiler(config.getCompilerPath(), config.isUseRlmake());
        if (looksLikePath(compiler) && !new File(compiler).isFile()) {
            throw new ExecutionException("Refal compiler not found at: " + compiler
                    + "\nFix the \"Refal compiler (rlc)\" field in the Run Configuration, or clear it to auto-detect.");
        }

        String file = config.getSourceFile();
        String workDir = workingDir(config);

        GeneralCommandLine cmd;
        if (config.isRunAfterCompile()) {
            String exe = StringUtil.isEmptyOrSpaces(config.getOutputExecutable())
                    ? defaultExecutable(file)
                    : config.getOutputExecutable().trim();
            String line = buildShellCommand(compiler, config.getCompilerOptions(), file, exe,
                    config.getProgramArguments(), SystemInfo.isWindows);
            cmd = SystemInfo.isWindows
                    ? new GeneralCommandLine("cmd.exe", "/c", line)
                    : new GeneralCommandLine("/bin/sh", "-c", line);
        } else {
            cmd = new GeneralCommandLine(compiler);
            cmd.getParametersList().addParametersString(config.getCompilerOptions());
            cmd.addParameter(file);
        }

        if (!StringUtil.isEmptyOrSpaces(workDir)) {
            cmd.setWorkDirectory(workDir);
        }
        cmd.setCharset(StandardCharsets.UTF_8);

        KillableColoredProcessHandler handler = new KillableColoredProcessHandler(cmd);
        ProcessTerminatedListener.attach(handler);
        handler.addProcessListener(new ProcessAdapter() {
            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                int code = event.getExitCode();
                if (code == 9009 || code == 127) {   // cmd.exe / POSIX sh: command not found
                    handler.notifyTextAvailable(commandNotFoundHint(code), ProcessOutputTypes.STDERR);
                }
            }
        });
        return handler;
    }

    /**
     * Explicit values are trusted as-is; a blank field or the legacy {@code "rlc"} auto-detects.
     * If detection finds nothing, returns literal {@code "rlc"} so the OS shell still gets its
     * chance (on Windows, PATHEXT lets cmd find {@code rlc.bat} even where our scan might not) —
     * a genuine "command not found" is then explained by the console hint, never blocked up front.
     */
    static @NotNull String resolveCompiler(@Nullable String configured, boolean useRlmake) {
        String c = configured == null ? "" : configured.trim();
        if (!c.isEmpty() && !"rlc".equals(c)) return c;
        String tool = useRlmake ? "rlmake" : "rlc";
        String detected = RefalCompilerLocator.detectTool(tool);
        return detected != null ? detected : tool;
    }

    /**
     * Pure command-line assembly (unit-tested): paths with spaces are quoted, empty parts are
     * skipped (no double spaces), and on Unix a bare executable name gets a {@code ./} prefix so
     * the shell finds it in the working directory.
     */
    static String buildShellCommand(String compiler, String options, String sourceFile,
                                    String exe, String programArgs, boolean windows) {
        StringBuilder sb = new StringBuilder();
        append(sb, q(compiler));
        append(sb, options == null ? "" : options.trim());
        append(sb, q(sourceFile));
        append(sb, "&&");
        append(sb, q(shellExecutable(exe, windows)));
        append(sb, programArgs == null ? "" : programArgs.trim());
        return sb.toString();
    }

    /** A bare name needs {@code ./} for the Unix shell; Windows cmd resolves the cwd itself. */
    static String shellExecutable(@Nullable String exe, boolean windows) {
        if (exe == null) return "";
        String e = exe.trim();
        if (windows || e.isEmpty()) return e;
        boolean bare = e.indexOf('/') < 0 && e.indexOf('\\') < 0;
        return bare ? "./" + e : e;
    }

    private static void append(StringBuilder sb, String part) {
        if (part == null || part.isEmpty()) return;
        if (sb.length() > 0) sb.append(' ');
        sb.append(part);
    }

    private static boolean looksLikePath(String s) {
        return s.indexOf('/') >= 0 || s.indexOf('\\') >= 0;
    }

    private static String parentOf(String file) {
        if (StringUtil.isEmptyOrSpaces(file)) return "";
        File parent = new File(file).getAbsoluteFile().getParentFile();
        return parent == null ? "" : parent.getPath();
    }

    /** Working directory: the configured one, or the source file's folder by default. */
    private static String workingDir(RefalRunConfiguration config) {
        return StringUtil.isEmptyOrSpaces(config.getWorkingDirectory())
                ? parentOf(config.getSourceFile())
                : config.getWorkingDirectory();
    }

    /** Default produced binary: the source base name (./name on *nix, name.exe on Windows). */
    private static String defaultExecutable(String file) {
        String name = new File(file).getName();
        int dot = name.lastIndexOf('.');
        if (dot > 0) name = name.substring(0, dot);
        return SystemInfo.isWindows ? name + ".exe" : "./" + name;
    }

    /** Minimal quoting for shell command building (paths with spaces). */
    private static String q(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty()) return "";
        return s.indexOf(' ') >= 0 ? '"' + s + '"' : s;
    }
}
