package com.github.refal5lambda.psi;

import com.github.refal5lambda.RefalFileType;
import com.github.refal5lambda.RefalTokenTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

/** Creates throwaway PSI leaves (parsed from tiny snippets) used when renaming. */
final class RefalElementFactory {
    private RefalElementFactory() {}

    static RefalFile createFile(Project project, String text) {
        return (RefalFile) PsiFileFactory.getInstance(project)
                .createFileFromText("_dummy.ref", RefalFileType.INSTANCE, text);
    }

    /** A FUNCTION_DEFINITION leaf carrying {@code name} (from a "name { }" snippet). */
    static @Nullable PsiElement createNameLeaf(Project project, String name) {
        return firstLeaf(createFile(project, name + " {\n}\n"), RefalTokenTypes.FUNCTION_DEFINITION);
    }

    /** A FUNCTION_CALL leaf carrying {@code name} (from a "&lt;name&gt;" snippet inside a function). */
    static @Nullable PsiElement createCallLeaf(Project project, String name) {
        return firstLeaf(createFile(project, "Dummy {\n  = <" + name + ">;\n}\n"), RefalTokenTypes.FUNCTION_CALL);
    }

    private static @Nullable PsiElement firstLeaf(PsiElement root, IElementType type) {
        if (root.getFirstChild() == null) {
            return root.getNode() != null && root.getNode().getElementType() == type ? root : null;
        }
        for (PsiElement c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
            PsiElement r = firstLeaf(c, type);
            if (r != null) return r;
        }
        return null;
    }
}
