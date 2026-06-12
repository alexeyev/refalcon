package com.github.refal5lambda.r2;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;

import java.util.EnumMap;
import java.util.Map;

/** IElementTypes for {@link Refal2Scanner.Kind}s plus the composite node types. */
public final class Refal2TokenTypes {
    private Refal2TokenTypes() {}

    private static final Map<Refal2Scanner.Kind, IElementType> BY_KIND = new EnumMap<>(Refal2Scanner.Kind.class);
    static {
        for (Refal2Scanner.Kind k : Refal2Scanner.Kind.values()) {
            BY_KIND.put(k, new IElementType("R2_" + k.name(), Refal2Language.INSTANCE));
        }
    }

    public static IElementType forKind(Refal2Scanner.Kind kind) {
        return BY_KIND.get(kind);
    }

    public static final IElementType COMMENT = forKind(Refal2Scanner.Kind.COMMENT);
    public static final IElementType STRING = forKind(Refal2Scanner.Kind.STRING);
    public static final IElementType KEYWORD = forKind(Refal2Scanner.Kind.KEYWORD);
    public static final IElementType DEF_NAME = forKind(Refal2Scanner.Kind.DEF_NAME);
    public static final IElementType FUNC = forKind(Refal2Scanner.Kind.FUNC);
    public static final IElementType IDENT = forKind(Refal2Scanner.Kind.IDENT);
    public static final IElementType LANGLE = forKind(Refal2Scanner.Kind.LANGLE);
    public static final IElementType RANGLE = forKind(Refal2Scanner.Kind.RANGLE);
    public static final IElementType KOPEN = forKind(Refal2Scanner.Kind.KOPEN);
    public static final IElementType DOT = forKind(Refal2Scanner.Kind.DOT);
    public static final IElementType LPAREN = forKind(Refal2Scanner.Kind.LPAREN);
    public static final IElementType RPAREN = forKind(Refal2Scanner.Kind.RPAREN);

    public static final IFileElementType FILE = new IFileElementType(Refal2Language.INSTANCE);
    public static final IElementType FUNCTION = new IElementType("R2_FUNCTION", Refal2Language.INSTANCE);
    public static final IElementType NAME = new IElementType("R2_NAME", Refal2Language.INSTANCE);
    public static final IElementType CALL = new IElementType("R2_CALL", Refal2Language.INSTANCE);

    public static final TokenSet COMMENTS = TokenSet.create(COMMENT);
    public static final TokenSet STRINGS = TokenSet.create(STRING);
    public static final TokenSet IDENTIFIERS = TokenSet.create(IDENT, FUNC, DEF_NAME);
}
