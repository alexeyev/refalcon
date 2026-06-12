package com.github.refal5lambda.r2;

import com.github.refal5lambda.RefalFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves a Refal-2 call to its definition. Refal-2 names are CASE-INSENSITIVE — the real
 * sources declare {@code extrn print} and call {@code <Print …>} — so resolution compares with
 * {@code equalsIgnoreCase}: same file first, then other Refal-2 files in the project. (Refal-2
 * corpora are tiny — the entire historic distribution is a handful of files — so the cross-file
 * pass scans directly instead of going through a cached index.)
 */
final class Refal2Reference extends PsiReferenceBase<PsiElement> {

    Refal2Reference(@NotNull PsiElement element) {
        super(element, new TextRange(0, element.getTextLength()), true);   // soft
    }

    @Override
    public @Nullable PsiElement resolve() {
        String name = getElement().getText();
        if (name == null || name.isEmpty()) return null;

        Refal2Function local = findInFile(getElement().getContainingFile(), name);
        if (local != null) return local;

        Project project = getElement().getProject();
        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile vf : FileTypeIndex.getFiles(RefalFileType.INSTANCE, GlobalSearchScope.projectScope(project))) {
            PsiFile file = psiManager.findFile(vf);
            if (!(file instanceof Refal2File) || file.equals(getElement().getContainingFile())) continue;
            Refal2Function fn = findInFile(file, name);
            if (fn != null) return fn;
        }
        return null;
    }

    private static @Nullable Refal2Function findInFile(@Nullable PsiFile file, @NotNull String name) {
        if (!(file instanceof Refal2File)) return null;
        for (Refal2Function fn : PsiTreeUtil.findChildrenOfType(file, Refal2Function.class)) {
            if (name.equalsIgnoreCase(fn.getName())) return fn;
        }
        return null;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newName) throws IncorrectOperationException {
        PsiElement leaf = getElement().getFirstChild();
        if (leaf != null) {
            leaf.replace(Refal2ElementFactory.createCallName(getElement().getProject(), newName));
        }
        return getElement();
    }
}
