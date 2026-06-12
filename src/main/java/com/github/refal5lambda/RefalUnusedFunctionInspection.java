package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalCall;
import com.github.refal5lambda.psi.RefalFunction;
import com.github.refal5lambda.psi.RefalTypes;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reports functions that are never called. Conservative by design:
 * <ul>
 *   <li>{@code $ENTRY} functions are exported API for other compilation units — never reported;</li>
 *   <li>{@code Go}/{@code GO} are program entry points — never reported;</li>
 *   <li>a function called anywhere in its own file (including only by itself) counts as used;</li>
 *   <li>otherwise a project-wide reference search must come up empty before reporting.</li>
 * </ul>
 * The in-file check is a single PSI pass; the project-wide search runs only for the (typically
 * few) candidates and is driven by the word index, so it scales with candidate files, not project
 * size.
 */
public final class RefalUnusedFunctionInspection extends LocalInspectionTool {

    @Override
    public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file,
                                                    @NotNull InspectionManager manager,
                                                    boolean isOnTheFly) {
        if (file.getLanguage() != RefalLanguage.INSTANCE) return null;

        Set<String> calledInFile = new HashSet<>();
        for (RefalCall call : PsiTreeUtil.findChildrenOfType(file, RefalCall.class)) {
            calledInFile.add(call.getText());
        }

        List<ProblemDescriptor> problems = new ArrayList<>();
        for (RefalFunction fn : PsiTreeUtil.findChildrenOfType(file, RefalFunction.class)) {
            String name = fn.getName();
            if (name == null || name.isEmpty()) continue;
            if ("Go".equals(name) || "GO".equals(name)) continue;   // program entry points
            if (isEntry(fn)) continue;                              // $ENTRY = exported API
            if (calledInFile.contains(name)) continue;
            if (ReferencesSearch.search(fn, GlobalSearchScope.projectScope(file.getProject()))
                    .findFirst() != null) {
                continue;
            }
            PsiElement target = fn.getNameIdentifier() != null ? fn.getNameIdentifier() : fn;
            problems.add(manager.createProblemDescriptor(target,
                    "Function '" + name + "' is never used",
                    true, ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly));
        }
        return problems.isEmpty() ? null : problems.toArray(new ProblemDescriptor[0]);
    }

    /** True if the function is immediately preceded by a {@code $ENTRY} directive. */
    private static boolean isEntry(RefalFunction fn) {
        PsiElement prev = fn.getPrevSibling();
        while (prev instanceof PsiWhiteSpace) prev = prev.getPrevSibling();
        return prev != null
                && PsiUtilCore.getElementType(prev) == RefalTypes.DIRECTIVE
                && prev.getText().trim().startsWith("$ENTRY");
    }
}
