package com.github.refal5lambda;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

/**
 * Comment support. Block comments {@code /* ... *}{@code /} work anywhere. The line-comment prefix
 * is {@code *}; Refal only treats {@code *} as a comment when it is in the FIRST column, and IntelliJ
 * places line comments at column 0 by default, so "Comment with Line Comment" matches Refal's rule.
 */
public final class RefalCommenter implements Commenter {
    @Nullable @Override public String getLineCommentPrefix() { return "*"; }
    @Nullable @Override public String getBlockCommentPrefix() { return "/*"; }
    @Nullable @Override public String getBlockCommentSuffix() { return "*/"; }
    @Nullable @Override public String getCommentedBlockCommentPrefix() { return null; }
    @Nullable @Override public String getCommentedBlockCommentSuffix() { return null; }
}
