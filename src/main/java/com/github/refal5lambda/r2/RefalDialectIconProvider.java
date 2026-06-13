package com.github.refal5lambda.r2;

import com.github.refal5lambda.RefalIcons;
import com.github.refal5lambda.psi.RefalFile;
import com.intellij.ide.IconProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Gives each Refal dialect its own file icon. Both dialects share the {@code .ref} file type, so
 * the icon cannot come from the file type — it is chosen per file from the language the
 * {@link RefalDialectSubstitutor} assigned: a distinct green "2" glyph for Refal-2, the blue glyph
 * for Refal-5/5lambda.
 */
public final class RefalDialectIconProvider extends IconProvider {

    @Override
    public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
        if (element instanceof Refal2File) return RefalIcons.FILE_R2;
        if (element instanceof RefalFile) return RefalIcons.FILE;
        return null;
    }
}
