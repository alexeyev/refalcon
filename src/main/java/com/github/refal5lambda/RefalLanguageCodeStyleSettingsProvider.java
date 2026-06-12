package com.github.refal5lambda;

import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Code-style defaults for Refal-5λ: 2-space indents, matching the style used throughout the
 * reference codebase (the bmstu-iu9/refal-5-lambda compiler sources). Continuation lines
 * (leading {@code =} / {@code ,}) get one extra level via the formatter's block model.
 */
public final class RefalLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    @Override
    public @NotNull Language getLanguage() {
        return RefalLanguage.INSTANCE;
    }

    @Override
    protected void customizeDefaults(@NotNull CommonCodeStyleSettings commonSettings,
                                     @NotNull CommonCodeStyleSettings.IndentOptions indentOptions) {
        indentOptions.INDENT_SIZE = 2;
        indentOptions.CONTINUATION_INDENT_SIZE = 4;
        indentOptions.TAB_SIZE = 2;
    }

    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return "* Refalcon formatting sample\n"
                + "$EXTERN Mul, Sub;\n"
                + "\n"
                + "$ENTRY Go {\n"
                + "  = <Prout <Fact 5>>;\n"
                + "}\n"
                + "\n"
                + "Fact {\n"
                + "  0 = 1;\n"
                + "  s.N\n"
                + "    = <Mul s.N <Fact <Sub s.N 1>>>;\n"
                + "}\n";
    }
}
