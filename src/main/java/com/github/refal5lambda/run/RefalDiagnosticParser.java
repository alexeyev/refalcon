package com.github.refal5lambda.run;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses compiler output into source diagnostics. IntelliJ-free, so it is unit-testable.
 *
 * <p>It only looks at output lines that mention a {@code .ref}/{@code .refi} source (so diagnostics
 * about generated C++ are ignored) and recognises the common shapes:
 * <pre>
 *   file.ref:LINE:COL: error|warning|note: message
 *   file.ref:LINE:COL: message
 *   file.ref:LINE: message
 * </pre>
 * If your {@code rlc} prints diagnostics differently, adjust the patterns below — that is the only
 * compiler-specific part of the inline-error feature.
 */
public final class RefalDiagnosticParser {
    private RefalDiagnosticParser() {}

    /** A single diagnostic. {@code column == 0} means "unknown column". Severity is error/warning/note. */
    public static final class Diagnostic {
        public final int line;
        public final int column;
        public final String severity;
        public final String message;

        public Diagnostic(int line, int column, String severity, String message) {
            this.line = line;
            this.column = column;
            this.severity = severity;
            this.message = message;
        }
    }

    private static final Pattern REF_FILE = Pattern.compile("\\.refi?\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern WITH_SEVERITY =
            Pattern.compile(":(\\d+):(\\d+):\\s*(error|warning|fatal error|note)\\b:?\\s*(.*)",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern LINE_COL = Pattern.compile(":(\\d+):(\\d+):\\s*(.*)");
    private static final Pattern LINE_ONLY = Pattern.compile(":(\\d+):\\s*(.*)");

    public static List<Diagnostic> parse(String output) {
        List<Diagnostic> result = new ArrayList<>();
        if (output == null || output.isEmpty()) return result;

        for (String raw : output.split("\\r?\\n")) {
            String line = raw.trim();
            if (line.isEmpty() || !REF_FILE.matcher(line).find()) continue;

            Matcher m = WITH_SEVERITY.matcher(line);
            if (m.find()) {
                result.add(new Diagnostic(parseInt(m.group(1)), parseInt(m.group(2)),
                        normalize(m.group(3)), m.group(4).trim()));
                continue;
            }
            m = LINE_COL.matcher(line);
            if (m.find()) {
                result.add(new Diagnostic(parseInt(m.group(1)), parseInt(m.group(2)),
                        "error", m.group(3).trim()));
                continue;
            }
            m = LINE_ONLY.matcher(line);
            if (m.find()) {
                result.add(new Diagnostic(parseInt(m.group(1)), 0, "error", m.group(2).trim()));
            }
        }
        return result;
    }

    private static String normalize(String severity) {
        String s = severity.toLowerCase();
        if (s.contains("warn")) return "warning";
        if (s.equals("note")) return "note";
        return "error";
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
