package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalFunction;
import com.github.refal5lambda.psi.RefalTypes;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * In Refal, sentences are tried IN ORDER, and a pattern consisting of a single e-variable
 * matches any argument. So a sentence like {@code e.X = …;} that is not the last one swallows
 * everything and the sentences below it can never fire — a classic ordering mistake.
 *
 * <p>The check is deliberately narrow: the pattern must consist of exactly one e-variable leaf
 * and nothing else. Anything extra — parentheses, more variables, symbols, or a where-clause
 * ({@code , … : …}) — means the sentence can fail and fall through, so it is not flagged.
 */
public final class RefalUnreachableSentenceInspection extends LocalInspectionTool {

    @Override
    public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file,
                                                    @NotNull InspectionManager manager,
                                                    boolean isOnTheFly) {
        if (file.getLanguage() != RefalLanguage.INSTANCE) return null;

        List<ProblemDescriptor> problems = new ArrayList<>();
        for (RefalFunction fn : PsiTreeUtil.findChildrenOfType(file, RefalFunction.class)) {
            ASTNode block = fn.getNode().findChildByType(RefalTypes.BLOCK);
            if (block == null) continue;
            ASTNode[] sentences = block.getChildren(TokenSet.create(RefalTypes.SENTENCE));
            for (int i = 0; i + 1 < sentences.length; i++) {        // last sentence may be anything
                PsiElement catchAll = catchAllVariable(sentences[i]);
                if (catchAll != null) {
                    problems.add(manager.createProblemDescriptor(catchAll,
                            "Catch-all pattern: the sentences below it are unreachable",
                            (com.intellij.codeInspection.LocalQuickFix) null,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly));
                }
            }
        }
        return problems.isEmpty() ? null : problems.toArray(new ProblemDescriptor[0]);
    }

    /** The pattern's single e-variable leaf, or {@code null} if the pattern is anything else. */
    private static @Nullable PsiElement catchAllVariable(ASTNode sentence) {
        ASTNode pattern = sentence.findChildByType(RefalTypes.PATTERN);
        if (pattern == null) return null;
        List<ASTNode> leaves = new ArrayList<>(2);
        collectMeaningfulLeaves(pattern, leaves);
        if (leaves.size() != 1) return null;
        ASTNode only = leaves.get(0);
        return only.getElementType() == RefalTokenTypes.E_VARIABLE ? only.getPsi() : null;
    }

    private static void collectMeaningfulLeaves(ASTNode node, List<ASTNode> out) {
        ASTNode child = node.getFirstChildNode();
        if (child == null) {
            PsiElement psi = node.getPsi();
            if (!(psi instanceof PsiWhiteSpace) && !(psi instanceof PsiComment)) {
                out.add(node);
            }
            return;
        }
        while (child != null && out.size() < 2) {       // two leaves already disqualify
            collectMeaningfulLeaves(child, out);
            child = child.getTreeNext();
        }
    }
}
