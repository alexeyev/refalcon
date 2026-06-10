package com.github.refal5lambda.psi;

import com.github.refal5lambda.RefalTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Lenient parser: it never reports syntax errors (so files never light up red). At the top level it
 * groups the token stream into FUNCTION nodes (name + block) and DIRECTIVE nodes. Inside a block it
 * models the structure: SENTENCE ({@code pattern = result ;}), with PATTERN/RESULT sides, EXPR for
 * parenthesized groups, CALL for the called name in an activation, and recursion into nested blocks.
 * Conditions/where-clauses are tolerated rather than fully modelled.
 */
public final class RefalParser implements PsiParser {

    @NotNull
    @Override
    public ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
        PsiBuilder.Marker rootMarker = builder.mark();
        while (!builder.eof()) {
            parseTopLevel(builder);
        }
        rootMarker.done(root);
        return builder.getTreeBuilt();
    }

    private void parseTopLevel(PsiBuilder b) {
        IElementType t = b.getTokenType();
        if (t == RefalTokenTypes.FUNCTION_DEFINITION) {
            parseFunction(b);
        } else if (t == RefalTokenTypes.KEYWORD || t == RefalTokenTypes.BAD_DIRECTIVE) {
            parseDirective(b);
        } else {
            b.advanceLexer(); // stray token: keep making progress
        }
    }

    private void parseFunction(PsiBuilder b) {
        PsiBuilder.Marker fn = b.mark();
        PsiBuilder.Marker name = b.mark();
        b.advanceLexer();                 // function name
        name.done(RefalTypes.NAME);
        if (b.getTokenType() == RefalTokenTypes.LBRACE) {
            parseBlock(b);
        }
        fn.done(RefalTypes.FUNCTION);
    }

    /** A block is a brace-delimited sequence of sentences. */
    private void parseBlock(PsiBuilder b) {
        PsiBuilder.Marker blk = b.mark();
        b.advanceLexer();                 // '{'
        while (!b.eof() && b.getTokenType() != RefalTokenTypes.RBRACE) {
            parseSentence(b);
        }
        if (b.getTokenType() == RefalTokenTypes.RBRACE) b.advanceLexer();
        blk.done(RefalTypes.BLOCK);
    }

    /**
     * A sentence is {@code pattern [= result] ;}. The pattern (left side) runs up to the first
     * top-level {@code =}; the result (right side) runs to the terminating {@code ;}. Conditions
     * and where-blocks are tolerated: their tokens are absorbed into the surrounding part (the
     * parser never reports an error), and any nested {@code { ... }} block is parsed recursively.
     */
    private void parseSentence(PsiBuilder b) {
        PsiBuilder.Marker sentence = b.mark();

        PsiBuilder.Marker pattern = b.mark();
        while (!b.eof()) {
            IElementType t = b.getTokenType();
            if (t == RefalTokenTypes.EQ || t == RefalTokenTypes.SEMICOLON || t == RefalTokenTypes.RBRACE) break;
            parseTerm(b);
        }
        pattern.done(RefalTypes.PATTERN);

        if (b.getTokenType() == RefalTokenTypes.EQ) {
            b.advanceLexer();             // '='
            PsiBuilder.Marker result = b.mark();
            while (!b.eof()) {
                IElementType t = b.getTokenType();
                if (t == RefalTokenTypes.SEMICOLON || t == RefalTokenTypes.RBRACE) break;
                parseTerm(b);
            }
            result.done(RefalTypes.RESULT);
        }

        if (b.getTokenType() == RefalTokenTypes.SEMICOLON) b.advanceLexer();
        sentence.done(RefalTypes.SENTENCE);
    }

    /** One term: a parenthesized group, an activation ({@code <…>}/{@code […]}), a nested block, or a single token. */
    private void parseTerm(PsiBuilder b) {
        IElementType t = b.getTokenType();
        if (t == RefalTokenTypes.LPAREN) {
            PsiBuilder.Marker expr = b.mark();
            b.advanceLexer();             // '('
            while (!b.eof() && b.getTokenType() != RefalTokenTypes.RPAREN && !isHardStop(b)) {
                parseTerm(b);
            }
            if (b.getTokenType() == RefalTokenTypes.RPAREN) b.advanceLexer();
            expr.done(RefalTypes.EXPR);
        } else if (t == RefalTokenTypes.LANGLE || t == RefalTokenTypes.LBRACK) {
            IElementType close = (t == RefalTokenTypes.LANGLE) ? RefalTokenTypes.RANGLE : RefalTokenTypes.RBRACK;
            b.advanceLexer();             // '<' or '['
            if (b.getTokenType() == RefalTokenTypes.FUNCTION_CALL) {
                PsiBuilder.Marker call = b.mark();   // name-only CALL so it can carry a reference
                b.advanceLexer();
                call.done(RefalTypes.CALL);
            }
            while (!b.eof() && b.getTokenType() != close && !isHardStop(b)) {
                parseTerm(b);
            }
            if (b.getTokenType() == close) b.advanceLexer();
        } else if (t == RefalTokenTypes.LBRACE) {
            parseBlock(b);                // nested block (functions-as-blocks)
        } else {
            b.advanceLexer();             // symbol, number, string, variable, comma, colon, stray, ...
        }
    }

    /** A closing brace is never part of a paren/activation, so treat it as a hard boundary on malformed input. */
    private static boolean isHardStop(PsiBuilder b) {
        return b.getTokenType() == RefalTokenTypes.RBRACE;
    }

    private void parseDirective(PsiBuilder b) {
        PsiBuilder.Marker dir = b.mark();
        b.advanceLexer();                 // the $word
        while (!b.eof()) {
            IElementType t = b.getTokenType();
            if (t == RefalTokenTypes.FUNCTION_DEFINITION || t == RefalTokenTypes.LBRACE) break;
            if (t == RefalTokenTypes.SEMICOLON) { b.advanceLexer(); break; }
            b.advanceLexer();
        }
        dir.done(RefalTypes.DIRECTIVE);
    }
}
