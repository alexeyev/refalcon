package com.github.refal5lambda.r2;

import com.github.refal5lambda.RefalFileType;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Navigate → Symbol… for Refal-2 functions. Unlike the λ contributor this scans directly
 * instead of going through a cached map: Refal-2 corpora are tiny (the whole historic
 * distribution is a handful of files), the scan is index-driven (FileTypeIndex), and it only
 * runs when the Symbol popup is open.
 */
public final class Refal2GotoSymbolContributor implements ChooseByNameContributor {

    @Override
    public String @NotNull [] getNames(Project project, boolean includeNonProjectItems) {
        if (project == null) return new String[0];
        Set<String> names = new LinkedHashSet<>();
        forEachFunction(project, (name, fn) -> names.add(name));
        return names.toArray(new String[0]);
    }

    @Override
    public NavigationItem @NotNull [] getItemsByName(String name, String pattern,
                                                     Project project, boolean includeNonProjectItems) {
        if (project == null || name == null) return new NavigationItem[0];
        List<NavigationItem> items = new ArrayList<>();
        forEachFunction(project, (fnName, fn) -> {
            if (name.equals(fnName)) items.add(fn);
        });
        return items.toArray(new NavigationItem[0]);
    }

    private static void forEachFunction(Project project, BiConsumer<String, Refal2Function> sink) {
        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile vf : FileTypeIndex.getFiles(RefalFileType.INSTANCE, GlobalSearchScope.projectScope(project))) {
            PsiFile file = psiManager.findFile(vf);
            if (!(file instanceof Refal2File)) continue;
            for (Refal2Function fn : PsiTreeUtil.findChildrenOfType(file, Refal2Function.class)) {
                String name = fn.getName();
                if (name != null && !name.isEmpty()) sink.accept(name, fn);
            }
        }
    }
}
