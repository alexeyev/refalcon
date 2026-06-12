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
 * Detects the Refal-2 dialect by file content — extensions cannot distinguish dialects because
 * both use {@code .ref} (verified: the historic refal2 distribution's own sources are .ref files).
 *
 * <p>Markers, all taken from real Refal-2 sources: a {@code name start} module header, bare
 * {@code entry}/{@code extrn} directives (Refal-5/5λ spell them {@code $ENTRY}/{@code $EXTERN}),
 * or a {@code k/name/} concretization call. Any {@code $}-directive vetoes the substitution —
 * dollar directives mean Refal-5/5λ.
 */
public final class RefalDialectSubstitutor extends LanguageSubstitutor {

    private static final Pattern DOLLAR_DIRECTIVE = Pattern.compile("\\$[A-Za-z]");
    private static final Pattern R2_MARKERS = Pattern.compile(
            "(?im)(^[ \\t]*[a-z_]\\w*[ \\t]+start[ \\t]*$)|(^[ \\t]*(entry|extrn)\\b)|(\\bk/[a-z_])");

    @Override
    public @Nullable Language getLanguage(@NotNull VirtualFile file, @NotNull Project project) {
        if (file.getFileType() != RefalFileType.INSTANCE) return null;
        String head = head(file);
        if (head == null || DOLLAR_DIRECTIVE.matcher(head).find()) return null;
        return R2_MARKERS.matcher(head).find() ? Refal2Language.INSTANCE : null;
    }

    private static @Nullable String head(VirtualFile file) {
        try (java.io.InputStream in = file.getInputStream()) {
            byte[] buf = new byte[4096];
            int n = 0, r;
            while (n < buf.length && (r = in.read(buf, n, buf.length - n)) > 0) n += r;
            return new String(buf, 0, n, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
}
