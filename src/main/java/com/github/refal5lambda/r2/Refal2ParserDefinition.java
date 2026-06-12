package com.github.refal5lambda.r2;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/**
 * Lenient Refal-2 parser: a {@code DEF_NAME} (identifier at column 0) opens a FUNCTION that runs
 * until the next definition or the {@code end} keyword — unless the next token is a keyword
 * ({@code name start} module headers are not functions). Every function-position word
 * ({@code <Name}, {@code k/name/}) is wrapped in a CALL node so references and navigation work.
 * Like the Refal-5λ parser, it never reports errors.
 */
public final class Refal2ParserDefinition implements ParserDefinition {

    @NotNull @Override
    public Lexer createLexer(Project project) {
        return new Refal2Lexer();
    }

    @NotNull @Override
    public PsiParser createParser(Project project) {
        return new Refal2Parser();
    }

    @NotNull @Override
    public IFileElementType getFileNodeType() {
        return Refal2TokenTypes.FILE;
    }

    @NotNull @Override
    public TokenSet getCommentTokens() {
        return Refal2TokenTypes.COMMENTS;
    }

    @NotNull @Override
    public TokenSet getStringLiteralElements() {
        return Refal2TokenTypes.STRINGS;
    }

    @NotNull @Override
    public PsiElement createElement(ASTNode node) {
        IElementType type = node.getElementType();
        if (type == Refal2TokenTypes.FUNCTION) return new Refal2Function(node);
        if (type == Refal2TokenTypes.CALL) return new Refal2Call(node);
        return new com.intellij.extapi.psi.ASTWrapperPsiElement(node);
    }

    @NotNull @Override
    public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new Refal2File(viewProvider);
    }

    static final class Refal2Parser implements PsiParser {
        @NotNull @Override
        public ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
            PsiBuilder.Marker file = builder.mark();
            PsiBuilder.Marker function = null;
            while (!builder.eof()) {
                IElementType t = builder.getTokenType();
                if (t == Refal2TokenTypes.DEF_NAME) {
                    if (function != null) { function.done(Refal2TokenTypes.FUNCTION); }
                    PsiBuilder.Marker m = builder.mark();
                    PsiBuilder.Marker name = builder.mark();
                    builder.advanceLexer();
                    name.done(Refal2TokenTypes.NAME);
                    if (builder.getTokenType() == Refal2TokenTypes.KEYWORD) {
                        m.drop();          // "name start" module header — not a function
                        function = null;
                    } else {
                        function = m;
                    }
                } else if (t == Refal2TokenTypes.FUNC) {
                    PsiBuilder.Marker call = builder.mark();
                    builder.advanceLexer();
                    call.done(Refal2TokenTypes.CALL);
                } else if (t == Refal2TokenTypes.KEYWORD && "end".equalsIgnoreCase(String.valueOf(builder.getTokenText()))) {
                    if (function != null) { function.done(Refal2TokenTypes.FUNCTION); function = null; }
                    builder.advanceLexer();
                } else {
                    builder.advanceLexer();
                }
            }
            if (function != null) function.done(Refal2TokenTypes.FUNCTION);
            file.done(root);
            return builder.getTreeBuilt();
        }
    }

    /** Convenience used by tests/utilities. */
    public static Language language() {
        return Refal2Language.INSTANCE;
    }
}
