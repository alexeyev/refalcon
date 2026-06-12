package com.github.refal5lambda.r2;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

/** Refal-2 has only '*' line comments — and only at column 0, like Refal-5/5λ. */
public final class Refal2Commenter implements Commenter {
    @Nullable @Override public String getLineCommentPrefix() { return "*"; }
    @Nullable @Override public String getBlockCommentPrefix() { return null; }
    @Nullable @Override public String getBlockCommentSuffix() { return null; }
    @Nullable @Override public String getCommentedBlockCommentPrefix() { return null; }
    @Nullable @Override public String getCommentedBlockCommentSuffix() { return null; }
}
