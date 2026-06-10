package com.github.refal5lambda;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class RefalElementType extends IElementType {
    public RefalElementType(@NotNull @NonNls String debugName) {
        super(debugName, RefalLanguage.INSTANCE);
    }
}
