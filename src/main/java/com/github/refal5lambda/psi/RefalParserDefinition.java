package com.github.refal5lambda.psi;

import com.github.refal5lambda.RefalLanguage;
import com.github.refal5lambda.RefalLexer;
import com.github.refal5lambda.RefalTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public final class RefalParserDefinition implements ParserDefinition {
    private static final IFileElementType FILE = new IFileElementType(RefalLanguage.INSTANCE);

    private static final TokenSet WHITESPACE = TokenSet.create(TokenType.WHITE_SPACE);
    // NATIVE_BLOCK (%% ... %%) is intentionally NOT a comment: it is embedded host code, so the
    // spellchecker and "comment" navigation shouldn't treat it as prose. The parser consumes it as
    // an ordinary leaf, and folding handles it explicitly by element type.
    private static final TokenSet COMMENTS = TokenSet.create(
            RefalTokenTypes.LINE_COMMENT, RefalTokenTypes.BLOCK_COMMENT);
    private static final TokenSet STRINGS = TokenSet.create(
            RefalTokenTypes.STRING_SINGLE, RefalTokenTypes.STRING_DOUBLE);

    @NotNull @Override
    public Lexer createLexer(Project project) {
        return new RefalLexer();
    }

    @NotNull @Override
    public PsiParser createParser(Project project) {
        return new RefalParser();
    }

    @NotNull @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull @Override
    public TokenSet getWhitespaceTokens() {
        return WHITESPACE;
    }

    @NotNull @Override
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull @Override
    public TokenSet getStringLiteralElements() {
        return STRINGS;
    }

    @NotNull @Override
    public PsiElement createElement(@NotNull ASTNode node) {
        return RefalTypes.createElement(node);
    }

    @NotNull @Override
    public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new RefalFile(viewProvider);
    }
}
