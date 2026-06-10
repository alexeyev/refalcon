package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalBlock;
import com.github.refal5lambda.psi.RefalTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class RefalFoldingBuilder extends FoldingBuilderEx implements DumbAware {

    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root,
                                                @NotNull Document document,
                                                boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        for (RefalBlock block : PsiTreeUtil.findChildrenOfType(root, RefalBlock.class)) {
            TextRange range = block.getTextRange();
            if (range.getLength() > 2) {
                descriptors.add(new FoldingDescriptor(block.getNode(), range));
            }
        }
        collectLeafFolds(root.getNode(), descriptors);
        return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY);
    }

    private static void collectLeafFolds(ASTNode node, List<FoldingDescriptor> out) {
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            IElementType type = child.getElementType();
            if ((type == RefalTokenTypes.BLOCK_COMMENT || type == RefalTokenTypes.NATIVE_BLOCK)
                    && child.getTextRange().getLength() > 4) {
                out.add(new FoldingDescriptor(child, child.getTextRange()));
            }
            collectLeafFolds(child, out);
        }
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        IElementType type = node.getElementType();
        if (type == RefalTypes.BLOCK) return "{...}";
        if (type == RefalTokenTypes.BLOCK_COMMENT) return "/*...*/";
        if (type == RefalTokenTypes.NATIVE_BLOCK) return "%%...%%";
        return "...";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
