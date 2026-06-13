package com.github.refal5lambda.r2;

import com.github.refal5lambda.RefalFileType;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Chooses the dialect for a {@code .ref} file. Extensions cannot distinguish dialects — both
 * Refal-5/5lambda and Refal-2 use {@code .ref} (verified against the historic refal2 distribution's
 * own sources) — and a {@link LanguageSubstitutor} must decide before parsing, so the decision is
 * made from the file's leading bytes. The earlier version matched raw text and misfired when a
 * Refal-2-looking token (notably {@code k/...}) appeared inside a string or comment. This version
 * is deliberately layered:
 *
 * <ol>
 *   <li><b>Explicit override.</b> A standalone comment {@code * refal-2} (or {@code * refal-5} /
 *       {@code * refal-5-lambda}), optionally written {@code * dialect: refal-2}, forces the
 *       dialect. This is the deterministic escape hatch for when detection would otherwise guess
 *       wrong, e.g. a Refal-2 fragment with no module header near the top.</li>
 *   <li><b>Strip strings and comments.</b> Single-quoted strings, column-0 {@code *} line
 *       comments and block comments are removed, so markers are only ever seen in real code.</li>
 *   <li><b>Strong Refal-5-family signals.</b> A {@code $}-directive ({@code $ENTRY},
 *       {@code $EXTERN}, ...) or a brace block means Refal-5/5lambda; Refal-2 has neither.</li>
 *   <li><b>Refal-2 markers</b> (in code): a {@code name start} module header, bare
 *       {@code entry}/{@code extrn} directives, or a {@code k/name/} concretization call.</li>
 * </ol>
 *
 * Anything else stays the default (Refal-5lambda).
 */
public final class RefalDialectSubstitutor extends LanguageSubstitutor {

    private static final int HEAD_BYTES = 8192;

    private static final Pattern OVERRIDE_R2 = Pattern.compile(
            "(?im)^[ \\t]*\\*[ \\t]*(dialect[ \\t]*[:=][ \\t]*)?refal[-_/ ]?2[ \\t]*$");
    private static final Pattern OVERRIDE_LAMBDA = Pattern.compile(
            "(?im)^[ \\t]*\\*[ \\t]*(dialect[ \\t]*[:=][ \\t]*)?refal[-_/ ]?5([-_ ]?lambda|\u03bb)?[ \\t]*$");

    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*[\\s\\S]*?\\*/");
    private static final Pattern LINE_COMMENT = Pattern.compile("(?m)^\\*.*$");
    private static final Pattern STRING = Pattern.compile("'[^'\\n]*'");

    private static final Pattern DOLLAR_DIRECTIVE = Pattern.compile("\\$[A-Za-z]");
    private static final Pattern R2_MARKERS = Pattern.compile(
            "(?im)(^[ \\t]*[a-z_]\\w*[ \\t]+start[ \\t]*$)|(^[ \\t]*(entry|extrn)\\b)|(\\bk/[a-z_])");

    @Override
    public @Nullable Language getLanguage(@NotNull VirtualFile file, @NotNull Project project) {
        if (file.getFileType() != RefalFileType.INSTANCE) return null;
        String head = head(file);
        if (head == null) return null;
        return detect(head);
    }

    /** Package-visible for tests: the whole decision, operating on already-read text. */
    static @Nullable Language detect(@NotNull String head) {
        if (OVERRIDE_R2.matcher(head).find()) return Refal2Language.INSTANCE;
        if (OVERRIDE_LAMBDA.matcher(head).find()) return null;

        String code = stripCommentsAndStrings(head);

        if (DOLLAR_DIRECTIVE.matcher(code).find()) return null;
        if (code.indexOf('{') >= 0) return null;

        if (R2_MARKERS.matcher(code).find()) return Refal2Language.INSTANCE;

        return null;
    }

    static @NotNull String stripCommentsAndStrings(@NotNull String text) {
        String t = BLOCK_COMMENT.matcher(text).replaceAll(" ");
        t = LINE_COMMENT.matcher(t).replaceAll(" ");
        t = STRING.matcher(t).replaceAll(" ");
        return t;
    }

    private static @Nullable String head(VirtualFile file) {
        try (java.io.InputStream in = file.getInputStream()) {
            byte[] buf = new byte[HEAD_BYTES];
            int n = 0, r;
            while (n < buf.length && (r = in.read(buf, n, buf.length - n)) > 0) n += r;
            return new String(buf, 0, n, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
}
