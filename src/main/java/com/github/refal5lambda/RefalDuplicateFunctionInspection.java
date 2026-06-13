package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalFunction;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reports a function name that is defined more than once in the same file. In Refal a function is
 * defined once (with all its sentences inside a single block); two separate definitions with the
 * same name is a redefinition error the compiler would reject — this surfaces it instantly in the
 * editor, with every clashing definition highlighted so the duplicates are easy to pair up.
 */
public final class RefalDuplicateFunctionInspection extends LocalInspectionTool {

    @Override
    public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file,
                                                    @NotNull InspectionManager manager,
                                                    boolean isOnTheFly) {
        if (file.getLanguage() != RefalLanguage.INSTANCE) return null;

        Map<String, List<RefalFunction>> byName = new LinkedHashMap<>();
        for (RefalFunction fn : PsiTreeUtil.findChildrenOfType(file, RefalFunction.class)) {
            String name = fn.getName();
            if (name == null || name.isEmpty()) continue;
            byName.computeIfAbsent(name, k -> new ArrayList<>()).add(fn);
        }

        List<ProblemDescriptor> problems = new ArrayList<>();
        for (Map.Entry<String, List<RefalFunction>> entry : byName.entrySet()) {
            List<RefalFunction> defs = entry.getValue();
            if (defs.size() < 2) continue;
            for (RefalFunction fn : defs) {
                PsiElement target = fn.getNameIdentifier() != null ? fn.getNameIdentifier() : fn;
                problems.add(manager.createProblemDescriptor(target,
                        "Function '" + entry.getKey() + "' is defined " + defs.size()
                                + " times in this file",
                        (LocalQuickFix) null,
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly));
            }
        }
        return problems.isEmpty() ? null : problems.toArray(new ProblemDescriptor[0]);
    }
}
