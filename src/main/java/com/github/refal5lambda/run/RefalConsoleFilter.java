package com.github.refal5lambda.run;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns compiler diagnostics that mention a Refal source location — e.g. {@code hello.ref:7:5: ...} —
 * into clickable links that jump to that line (and column). Best-effort: it matches a
 * {@code .ref}/{@code .refi} path followed by a line and an optional column; relative paths are
 * resolved against the run configuration's working directory.
 */
final class RefalConsoleFilter implements Filter {
    /** Visible for tests. The optional drive-letter prefix lets absolute Windows paths (C:\...) link too. */
    static final Pattern LOCATION =
            Pattern.compile("((?:[A-Za-z]:)?[^\\s:]+\\.refi?):(\\d+)(?::(\\d+))?");

    private final Project project;
    private final @Nullable String baseDir;

    RefalConsoleFilter(@NotNull Project project, @Nullable String baseDir) {
        this.project = project;
        this.baseDir = baseDir;
    }

    @Nullable
    @Override
    public Result applyFilter(@NotNull String line, int entireLength) {
        Matcher m = LOCATION.matcher(line);
        if (!m.find()) return null;

        VirtualFile file = resolve(m.group(1));
        if (file == null) return null;

        int lineNumber = parseIndex(m.group(2));                       // 1-based -> 0-based
        int column = m.group(3) != null ? parseIndex(m.group(3)) : 0;  // 1-based -> 0-based

        int lineStart = entireLength - line.length();
        int start = lineStart + m.start();
        int end = lineStart + m.end();
        return new Result(start, end, new OpenFileHyperlinkInfo(project, file, lineNumber, column));
    }

    private static int parseIndex(String s) {
        try {
            return Math.max(0, Integer.parseInt(s) - 1);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Nullable
    private VirtualFile resolve(String path) {
        File f = new File(path);
        if (!f.isAbsolute() && !StringUtil.isEmptyOrSpaces(baseDir)) {
            f = new File(baseDir, path);
        }
        return LocalFileSystem.getInstance()
                .findFileByPath(f.getAbsolutePath().replace(File.separatorChar, '/'));
    }
}
