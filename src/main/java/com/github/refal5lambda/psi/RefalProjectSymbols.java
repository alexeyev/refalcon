package com.github.refal5lambda.psi;

import com.github.refal5lambda.RefalFileType;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A project-wide map {@code function name -> definitions}, built lazily and cached until the next
 * PSI change ({@link PsiModificationTracker#MODIFICATION_COUNT}).
 *
 * <p>Why: cross-file navigation used to rescan (and parse) every Refal file in the project on
 * EVERY reference resolve — {@code PsiReferenceBase.resolve()} is not cached by the platform.
 * With this map the project is walked once per change, and all resolves, navigation and the
 * "Go to Symbol" popup share the result. Smart pointers keep the cached elements valid across
 * reparses.
 */
public final class RefalProjectSymbols {

    private RefalProjectSymbols() {}

    /** All function definitions in the project, by name. Call inside a read action. */
    public static @NotNull Map<String, List<SmartPsiElementPointer<RefalFunction>>> get(@NotNull Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(project, () ->
                CachedValueProvider.Result.create(build(project), PsiModificationTracker.MODIFICATION_COUNT));
    }

    private static Map<String, List<SmartPsiElementPointer<RefalFunction>>> build(Project project) {
        Map<String, List<SmartPsiElementPointer<RefalFunction>>> map = new HashMap<>();
        PsiManager psiManager = PsiManager.getInstance(project);
        SmartPointerManager pointers = SmartPointerManager.getInstance(project);
        for (VirtualFile vf : FileTypeIndex.getFiles(RefalFileType.INSTANCE, GlobalSearchScope.projectScope(project))) {
            ProgressManager.checkCanceled();   // project walk must stay cancellable (typed-action responsiveness)
            PsiFile file = psiManager.findFile(vf);
            if (!(file instanceof RefalFile)) continue;
            for (RefalFunction fn : PsiTreeUtil.findChildrenOfType(file, RefalFunction.class)) {
                String name = fn.getName();
                if (name == null || name.isEmpty()) continue;
                map.computeIfAbsent(name, k -> new ArrayList<>(1))
                        .add(pointers.createSmartPsiElementPointer(fn));
            }
        }
        return map;
    }
}
