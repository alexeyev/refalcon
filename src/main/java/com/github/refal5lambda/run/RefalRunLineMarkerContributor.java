package com.github.refal5lambda.run;

import com.github.refal5lambda.RefalTokenTypes;
import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shows a green "run" arrow in the gutter next to the program entry function ({@code Go} / {@code GO}),
 * which is the conventional entry point of a Refal-5 Lambda program. Clicking it creates and runs a
 * Refal run configuration for the file (with the compiler auto-detected when possible).
 */
public final class RefalRunLineMarkerContributor extends RunLineMarkerContributor {

    private static final Function<PsiElement, String> TOOLTIP = element -> "Run Refal program";

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement element) {
        // The platform requires Info to be produced for leaf elements only.
        if (element.getFirstChild() != null) return null;
        if (PsiUtilCore.getElementType(element) != RefalTokenTypes.FUNCTION_DEFINITION) return null;
        String name = element.getText();
        if (!"Go".equals(name) && !"GO".equals(name)) return null;
        return new Info(AllIcons.RunConfigurations.TestState.Run, ExecutorAction.getActions(0), TOOLTIP);
    }
}
