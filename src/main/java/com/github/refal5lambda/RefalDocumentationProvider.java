package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalCall;
import com.github.refal5lambda.psi.RefalFile;
import com.github.refal5lambda.psi.RefalFunction;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Ctrl+Q / hover documentation for built-in functions and for functions defined in the file. */
public final class RefalDocumentationProvider extends AbstractDocumentationProvider {

    @Nullable
    @Override
    public PsiElement getCustomDocumentationElement(@NotNull Editor editor, @NotNull PsiFile file,
                                                    @Nullable PsiElement contextElement, int targetOffset) {
        if (contextElement == null) return null;
        IElementType type = PsiUtilCore.getElementType(contextElement);
        if (type == RefalTokenTypes.FUNCTION_CALL || type == RefalTokenTypes.FUNCTION_DEFINITION) {
            return contextElement;
        }
        return null;
    }

    @Nullable
    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        String name = nameOf(element);
        if (name == null) return null;

        String builtin = RefalBuiltins.describe(name);
        if (builtin != null) {
            return DocumentationMarkup.DEFINITION_START + "built-in function <b>" + esc(name) + "</b>"
                    + DocumentationMarkup.DEFINITION_END
                    + DocumentationMarkup.CONTENT_START + esc(builtin) + DocumentationMarkup.CONTENT_END;
        }
        RefalFunction fn = findFunction(element, name);
        if (fn != null) {
            return DocumentationMarkup.DEFINITION_START + "function <b>" + esc(name) + "</b>"
                    + DocumentationMarkup.DEFINITION_END
                    + DocumentationMarkup.CONTENT_START + "<pre>" + esc(firstLine(fn.getText())) + "</pre>"
                    + DocumentationMarkup.CONTENT_END;
        }
        return null;
    }

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        String name = nameOf(element);
        if (name == null) return null;
        if (RefalBuiltins.describe(name) != null) return "built-in function " + name;
        return findFunction(element, name) != null ? "function " + name : null;
    }

    @Nullable
    private static String nameOf(PsiElement element) {
        if (element instanceof RefalFunction) return ((RefalFunction) element).getName();
        if (element instanceof RefalCall) return element.getText();
        IElementType type = PsiUtilCore.getElementType(element);
        if (type == RefalTokenTypes.FUNCTION_CALL || type == RefalTokenTypes.FUNCTION_DEFINITION) {
            return element.getText();
        }
        return null;
    }

    @Nullable
    private static RefalFunction findFunction(PsiElement context, String name) {
        PsiFile file = context.getContainingFile();
        if (!(file instanceof RefalFile)) return null;
        for (RefalFunction fn : PsiTreeUtil.findChildrenOfType(file, RefalFunction.class)) {
            if (name.equals(fn.getName())) return fn;
        }
        return null;
    }

    private static String firstLine(String text) {
        int nl = text.indexOf('\n');
        return (nl >= 0 ? text.substring(0, nl) : text).trim();
    }

    private static String esc(String s) {
        return StringUtil.escapeXmlEntities(s);
    }
}
