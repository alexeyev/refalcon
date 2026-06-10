package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalFunction;
import com.github.refal5lambda.psi.RefalTypes;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Basic completion: directive keywords, common built-ins, function names from the file, and the
 * variables in scope (the {@code s.}/{@code t.}/{@code e.} variables of the enclosing sentence, or
 * failing that the enclosing function).
 */
public final class RefalCompletionContributor extends CompletionContributor {
    public RefalCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(RefalLanguage.INSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        for (String d : RefalBuiltins.DIRECTIVES) {
                            result.addElement(LookupElementBuilder.create(d)
                                    .withIcon(AllIcons.Nodes.Tag).bold());
                        }
                        for (String f : RefalBuiltins.FUNCTIONS) {
                            result.addElement(LookupElementBuilder.create(f)
                                    .withIcon(AllIcons.Nodes.Function)
                                    .withTypeText("builtin", true));
                        }
                        PsiFile file = parameters.getOriginalFile();
                        for (RefalFunction fn : PsiTreeUtil.findChildrenOfType(file, RefalFunction.class)) {
                            String n = fn.getName();
                            if (n != null && !n.isEmpty()) {
                                result.addElement(LookupElementBuilder.create(n)
                                        .withIcon(AllIcons.Nodes.Function));
                            }
                        }
                        // Variables in scope at the caret.
                        Set<String> vars = new LinkedHashSet<>();
                        collectVariables(enclosingScope(parameters.getPosition()), vars);
                        for (String v : vars) {
                            result.addElement(LookupElementBuilder.create(v)
                                    .withIcon(AllIcons.Nodes.Variable)
                                    .withTypeText("variable", true));
                        }
                    }
                });
    }

    /** Nearest enclosing sentence (preferred) or function of the given element. */
    @Nullable
    private static PsiElement enclosingScope(@Nullable PsiElement element) {
        PsiElement function = null;
        for (PsiElement e = element; e != null && !(e instanceof PsiFile); e = e.getParent()) {
            IElementType t = PsiUtilCore.getElementType(e);
            if (t == RefalTypes.SENTENCE) return e;
            if (t == RefalTypes.FUNCTION && function == null) function = e;
        }
        return function;
    }

    private static void collectVariables(@Nullable PsiElement scope, Set<String> out) {
        if (scope == null) return;
        if (scope.getFirstChild() == null) {
            IElementType t = PsiUtilCore.getElementType(scope);
            if (t == RefalTokenTypes.S_VARIABLE || t == RefalTokenTypes.T_VARIABLE
                    || t == RefalTokenTypes.E_VARIABLE) {
                String text = scope.getText();
                if (!text.isEmpty()) out.add(text);
            }
            return;
        }
        for (PsiElement c = scope.getFirstChild(); c != null; c = c.getNextSibling()) {
            collectVariables(c, out);
        }
    }
}
