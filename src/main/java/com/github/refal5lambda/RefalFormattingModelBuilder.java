package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalTypes;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Reformat Code for Refal-5λ. Conservative by design: it normalizes indentation and the spacing
 * around structural tokens, and otherwise <b>preserves the author's line structure</b> (it never
 * joins or splits lines except to glue {@code ;} and {@code ,} prefixes and {@code Name {}
 * headers), because the canonical Refal style routinely starts lines with {@code =} and {@code ,}:
 * <pre>
 *   F {
 *     e.X
 *       , &lt;Check e.X&gt; : True
 *       = &lt;Process e.X&gt;;
 *   }
 * </pre>
 *
 * <p><b>Refal-specific safety rule:</b> {@code *} line comments are comments <i>only at column
 * 0</i> — indenting one would silently turn it into code. They (and {@code %% native %%} blocks)
 * are therefore pinned with an absolute-none indent.
 */
public final class RefalFormattingModelBuilder implements FormattingModelBuilder {

    @Override
    public @NotNull FormattingModel createModel(@NotNull FormattingContext context) {
        CodeStyleSettings settings = context.getCodeStyleSettings();
        RefalBlock root = new RefalBlock(context.getContainingFile().getNode(),
                Indent.getNoneIndent(), createSpacingBuilder(settings));
        return FormattingModelProvider.createFormattingModelForPsiFile(
                context.getContainingFile(), root, settings);
    }

    private static SpacingBuilder createSpacingBuilder(CodeStyleSettings settings) {
        return new SpacingBuilder(settings, RefalLanguage.INSTANCE)
                // '=' between pattern and result: one space, but a line break before it is style.
                .around(RefalTokenTypes.EQ).spacing(1, 1, 0, true, 0)
                // ';' hugs the result, never starts a line.
                .before(RefalTokenTypes.SEMICOLON).spacing(0, 0, 0, false, 0)
                // ',' may start a continuation line (canonical condition style); never preceded
                // by a space when inline, always followed by one.
                .before(RefalTokenTypes.COMMA).spacing(0, 0, 0, true, 0)
                .after(RefalTokenTypes.COMMA).spacing(1, 1, 0, true, 0)
                .around(RefalTokenTypes.COLON).spacing(1, 1, 0, true, 0)
                // Activation and structure brackets carry no inner padding.
                .after(RefalTokenTypes.LANGLE).spacing(0, 0, 0, true, 0)
                .before(RefalTokenTypes.RANGLE).spacing(0, 0, 0, true, 0)
                .after(RefalTokenTypes.LPAREN).spacing(0, 0, 0, true, 0)
                .before(RefalTokenTypes.RPAREN).spacing(0, 0, 0, true, 0)
                // 'Name {' on one line; '{ x' / 'x }' padded when a body shares the line.
                .between(RefalTypes.NAME, RefalTypes.BLOCK).spacing(1, 1, 0, false, 0)
                .afterInside(RefalTokenTypes.LBRACE, RefalTypes.BLOCK).spacing(1, 1, 0, true, 2)
                .beforeInside(RefalTokenTypes.RBRACE, RefalTypes.BLOCK).spacing(1, 1, 0, true, 2)
                // '$ENTRY Go {' when on one line; kept apart when the author broke the line.
                .between(RefalTypes.DIRECTIVE, RefalTypes.FUNCTION).spacing(1, 1, 0, true, 1);
    }

    /** One block per meaningful AST node; indents are computed from the parent node kind. */
    static final class RefalBlock extends AbstractBlock {

        private final Indent indent;
        private final SpacingBuilder spacing;

        RefalBlock(@NotNull ASTNode node, @NotNull Indent indent, @NotNull SpacingBuilder spacing) {
            super(node, null, null);
            this.indent = indent;
            this.spacing = spacing;
        }

        @Override
        protected List<Block> buildChildren() {
            List<Block> blocks = new ArrayList<>();
            boolean first = true;
            for (ASTNode child = myNode.getFirstChildNode(); child != null; child = child.getTreeNext()) {
                if (child.getElementType() == TokenType.WHITE_SPACE || child.getTextLength() == 0) continue;
                blocks.add(new RefalBlock(child, childIndent(child, first), spacing));
                first = false;
            }
            return blocks;
        }

        private Indent childIndent(ASTNode child, boolean firstMeaningful) {
            IElementType type = child.getElementType();
            // '*' comments are comments ONLY at column 0; '%% ... %%' is verbatim C++.
            // Indenting either would change program meaning, so pin them to the left margin.
            if (type == RefalTokenTypes.LINE_COMMENT || type == RefalTokenTypes.NATIVE_BLOCK) {
                return Indent.getAbsoluteNoneIndent();
            }
            IElementType parent = myNode.getElementType();
            if (parent == RefalTypes.BLOCK) {
                return (type == RefalTokenTypes.LBRACE || type == RefalTokenTypes.RBRACE)
                        ? Indent.getNoneIndent()
                        : Indent.getNormalIndent();
            }
            if (parent == RefalTypes.SENTENCE || parent == RefalTypes.PATTERN) {
                // Continuation lines of a sentence — leading '=' as a direct sentence child, or a
                // leading ',' condition which the lenient parser nests inside PATTERN — indent one
                // extra level past the sentence start.
                return firstMeaningful ? Indent.getNoneIndent() : Indent.getNormalIndent();
            }
            return Indent.getNoneIndent();
        }

        @Override
        public Indent getIndent() {
            return indent;
        }

        @Override
        public @NotNull ChildAttributes getChildAttributes(int newChildIndex) {
            IElementType type = myNode.getElementType();
            if (type == RefalTypes.BLOCK || type == RefalTypes.SENTENCE) {
                return new ChildAttributes(Indent.getNormalIndent(), null);
            }
            return new ChildAttributes(Indent.getNoneIndent(), null);
        }

        @Override
        public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
            return spacing.getSpacing(this, child1, child2);
        }

        @Override
        public boolean isLeaf() {
            return myNode.getFirstChildNode() == null;
        }
    }
}
