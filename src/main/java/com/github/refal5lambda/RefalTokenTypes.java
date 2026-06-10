package com.github.refal5lambda;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

/** IElementType instances for each {@link RefalTokenKind}, plus the mapping used by the lexer. */
public final class RefalTokenTypes {
    private RefalTokenTypes() {}

    public static final IElementType LINE_COMMENT          = new RefalElementType("LINE_COMMENT");
    public static final IElementType BLOCK_COMMENT         = new RefalElementType("BLOCK_COMMENT");
    public static final IElementType NATIVE_BLOCK          = new RefalElementType("NATIVE_BLOCK");
    public static final IElementType STRING_SINGLE         = new RefalElementType("STRING_SINGLE");
    public static final IElementType STRING_DOUBLE         = new RefalElementType("STRING_DOUBLE");
    public static final IElementType STRING_ESCAPE_VALID   = new RefalElementType("STRING_ESCAPE_VALID");
    public static final IElementType STRING_ESCAPE_INVALID = new RefalElementType("STRING_ESCAPE_INVALID");
    public static final IElementType NUMBER                = new RefalElementType("NUMBER");
    public static final IElementType KEYWORD               = new RefalElementType("KEYWORD");
    public static final IElementType BAD_DIRECTIVE         = new RefalElementType("BAD_DIRECTIVE");
    public static final IElementType S_VARIABLE            = new RefalElementType("S_VARIABLE");
    public static final IElementType T_VARIABLE            = new RefalElementType("T_VARIABLE");
    public static final IElementType E_VARIABLE            = new RefalElementType("E_VARIABLE");
    public static final IElementType FUNCTION_DEFINITION   = new RefalElementType("FUNCTION_DEFINITION");
    public static final IElementType FUNCTION_CALL         = new RefalElementType("FUNCTION_CALL");
    public static final IElementType IDENTIFIER            = new RefalElementType("IDENTIFIER");
    public static final IElementType LPAREN                = new RefalElementType("LPAREN");
    public static final IElementType RPAREN                = new RefalElementType("RPAREN");
    public static final IElementType LBRACE                = new RefalElementType("LBRACE");
    public static final IElementType RBRACE                = new RefalElementType("RBRACE");
    public static final IElementType LANGLE                = new RefalElementType("LANGLE");
    public static final IElementType RANGLE                = new RefalElementType("RANGLE");
    public static final IElementType LBRACK                = new RefalElementType("LBRACK");
    public static final IElementType RBRACK                = new RefalElementType("RBRACK");
    public static final IElementType SEMICOLON             = new RefalElementType("SEMICOLON");
    public static final IElementType COMMA                 = new RefalElementType("COMMA");
    public static final IElementType COLON                 = new RefalElementType("COLON");
    public static final IElementType EQ                    = new RefalElementType("EQ");
    public static final IElementType OTHER                 = new RefalElementType("OTHER");

    public static IElementType forKind(RefalTokenKind kind) {
        switch (kind) {
            case WHITE_SPACE:           return TokenType.WHITE_SPACE;
            case LINE_COMMENT:          return LINE_COMMENT;
            case BLOCK_COMMENT:         return BLOCK_COMMENT;
            case NATIVE_BLOCK:          return NATIVE_BLOCK;
            case STRING_SINGLE:         return STRING_SINGLE;
            case STRING_DOUBLE:         return STRING_DOUBLE;
            case STRING_ESCAPE_VALID:   return STRING_ESCAPE_VALID;
            case STRING_ESCAPE_INVALID: return STRING_ESCAPE_INVALID;
            case NUMBER:                return NUMBER;
            case KEYWORD:               return KEYWORD;
            case BAD_DIRECTIVE:         return BAD_DIRECTIVE;
            case S_VARIABLE:            return S_VARIABLE;
            case T_VARIABLE:            return T_VARIABLE;
            case E_VARIABLE:            return E_VARIABLE;
            case FUNCTION_DEFINITION:   return FUNCTION_DEFINITION;
            case FUNCTION_CALL:         return FUNCTION_CALL;
            case IDENTIFIER:            return IDENTIFIER;
            case LPAREN:                return LPAREN;
            case RPAREN:                return RPAREN;
            case LBRACE:                return LBRACE;
            case RBRACE:                return RBRACE;
            case LANGLE:                return LANGLE;
            case RANGLE:                return RANGLE;
            case LBRACK:                return LBRACK;
            case RBRACK:                return RBRACK;
            case SEMICOLON:             return SEMICOLON;
            case COMMA:                 return COMMA;
            case COLON:                 return COLON;
            case EQ:                    return EQ;
            case OTHER:
            default:                    return OTHER;
        }
    }
}
