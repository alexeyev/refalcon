package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalFunction;
import com.github.refal5lambda.psi.RefalProjectSymbols;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Makes Refal functions appear in <i>Navigate → Symbol…</i> (Ctrl+Alt+Shift+N /
 * Cmd+Opt+O) — jump to any function in the project by (fuzzy) name. Backed by the same cached
 * project-wide symbol map that reference resolution uses, so listing names costs one map read.
 */
public final class RefalGotoSymbolContributor implements ChooseByNameContributor {

    @Override
    public String @NotNull [] getNames(Project project, boolean includeNonProjectItems) {
        if (project == null) return new String[0];
        return RefalProjectSymbols.get(project).keySet().toArray(new String[0]);
    }

    @Override
    public NavigationItem @NotNull [] getItemsByName(String name, String pattern,
                                                     Project project, boolean includeNonProjectItems) {
        if (project == null || name == null) return new NavigationItem[0];
        Map<String, List<SmartPsiElementPointer<RefalFunction>>> map = RefalProjectSymbols.get(project);
        List<SmartPsiElementPointer<RefalFunction>> defs = map.get(name);
        if (defs == null) return new NavigationItem[0];
        List<NavigationItem> items = new ArrayList<>(defs.size());
        for (SmartPsiElementPointer<RefalFunction> pointer : defs) {
            RefalFunction fn = pointer.getElement();
            if (fn != null) items.add(fn);
        }
        return items.toArray(new NavigationItem[0]);
    }
}
