package com.github.refal5lambda;

/** Token kinds produced by {@link RefalScanner}. Pure enum, no IntelliJ dependencies. */
public enum RefalTokenKind {
    WHITE_SPACE,
    LINE_COMMENT,
    BLOCK_COMMENT,
    NATIVE_BLOCK,
    STRING_SINGLE,          // ' , plain text inside a '...' string, and its closing '
    STRING_DOUBLE,          // " , plain text inside a "..." string, and its closing "
    STRING_ESCAPE_VALID,    // \n \r \t \\ \' \" \< \> \( \) \xHH
    STRING_ESCAPE_INVALID,  // any other backslash sequence
    NUMBER,
    KEYWORD,
    BAD_DIRECTIVE,
    S_VARIABLE,
    T_VARIABLE,
    E_VARIABLE,
    FUNCTION_DEFINITION,
    FUNCTION_CALL,
    IDENTIFIER,
    LPAREN, RPAREN,
    LBRACE, RBRACE,
    LANGLE, RANGLE,
    LBRACK, RBRACK,
    SEMICOLON,
    COMMA,
    COLON,
    EQ,
    OTHER
}
