package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalFile;
import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import org.jetbrains.annotations.NotNull;

/** Makes the Refal live templates available inside .ref files. */
public final class RefalTemplateContextType extends TemplateContextType {
    public RefalTemplateContextType() {
        super("Refal");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        return templateActionContext.getFile() instanceof RefalFile;
    }
}
