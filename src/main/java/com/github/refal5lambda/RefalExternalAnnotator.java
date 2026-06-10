package com.github.refal5lambda;

import com.github.refal5lambda.run.RefalCompilerLocator;
import com.github.refal5lambda.run.RefalDiagnosticParser;
import com.github.refal5lambda.run.RefalDiagnosticParser.Diagnostic;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Runs the Refal-5λ compiler ({@code rlc}) in the background and shows its diagnostics inline.
 *
 * <p>The current editor text is written to a private temp file and checked there with
 * {@code rlc --grammar-check} — a fast syntax-only pass that produces no build artifacts and does
 * not require a C++ toolchain (verified against the real compiler: diagnostics keep the
 * {@code file:line:col: ERROR: …} format that {@link RefalDiagnosticParser} reads, exit code is
 * 0/1, and nothing is written next to the source). The project tree is never touched and unsaved
 * edits are checked. If {@code rlc} is not found, or the installed {@code rlc} predates
 * {@code --grammar-check}, the annotator silently shows nothing — it never disrupts the editor.
 */
public final class RefalExternalAnnotator
        extends ExternalAnnotator<RefalExternalAnnotator.Request, List<Diagnostic>> {

    /** Captured on the EDT before the background run. */
    public static final class Request {
        final String text;
        final Project project;
        Request(String text, Project project) {
            this.text = text;
            this.project = project;
        }
    }

    private static final int TIMEOUT_MS = 15_000;

    @Nullable
    @Override
    public Request collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        return new Request(editor.getDocument().getText(), file.getProject());
    }

    @Nullable
    @Override
    public List<Diagnostic> doAnnotate(Request request) {
        if (request == null) return Collections.emptyList();
        String compiler = RefalCompilerLocator.detect();
        if (compiler == null) return Collections.emptyList();

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("refal-check");
            Path source = tempDir.resolve("check.ref");
            Files.write(source, request.text.getBytes(StandardCharsets.UTF_8));

            GeneralCommandLine commandLine = new GeneralCommandLine(compiler, "--grammar-check", source.toString());
            commandLine.setWorkDirectory(tempDir.toFile());
            commandLine.setCharset(StandardCharsets.UTF_8);

            ProcessOutput output = new CapturingProcessHandler(commandLine).runProcess(TIMEOUT_MS, true);
            return RefalDiagnosticParser.parse(output.getStdout() + "\n" + output.getStderr());
        } catch (Exception e) {
            return Collections.emptyList();   // fail-safe: never disrupt the editor
        } finally {
            deleteQuietly(tempDir);
        }
    }

    @Override
    public void apply(@NotNull PsiFile file, List<Diagnostic> diagnostics, @NotNull AnnotationHolder holder) {
        if (diagnostics == null || diagnostics.isEmpty()) return;
        Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        if (document == null) return;

        for (Diagnostic d : diagnostics) {
            TextRange range = toRange(document, d.line, d.column);
            if (range == null) continue;
            String message = d.message.isEmpty() ? "rlc" : d.message;
            holder.newAnnotation(severityOf(d.severity), message).range(range).create();
        }
    }

    private static HighlightSeverity severityOf(String severity) {
        if ("warning".equals(severity)) return HighlightSeverity.WARNING;
        if ("note".equals(severity)) return HighlightSeverity.WEAK_WARNING;
        return HighlightSeverity.ERROR;
    }

    @Nullable
    private static TextRange toRange(Document document, int line1Based, int column1Based) {
        int line = line1Based - 1;
        if (line < 0 || line >= document.getLineCount()) return null;
        int lineStart = document.getLineStartOffset(line);
        int lineEnd = document.getLineEndOffset(line);
        int start = column1Based > 0 ? Math.min(lineStart + (column1Based - 1), lineEnd) : lineStart;
        int end = Math.max(start + 1, lineEnd);
        end = Math.min(end, document.getTextLength());
        start = Math.min(start, document.getTextLength());
        return start < end ? new TextRange(start, end) : null;
    }

    private static void deleteQuietly(@Nullable Path dir) {
        if (dir == null) return;
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // best effort
                }
            });
        } catch (IOException ignored) {
            // best effort
        }
    }
}
