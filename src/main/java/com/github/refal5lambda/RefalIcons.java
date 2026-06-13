package com.github.refal5lambda;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

public final class RefalIcons {
    public static final Icon FILE = IconLoader.getIcon("/icons/refal.svg", RefalIcons.class);
    /** Refal-2 dialect file icon (green glyph with a "2" badge). */
    public static final Icon FILE_R2 = IconLoader.getIcon("/icons/refal2.svg", RefalIcons.class);

    private RefalIcons() {}
}
