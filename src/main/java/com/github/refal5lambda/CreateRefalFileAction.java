package com.github.refal5lambda;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

/** Adds "Refal File" to the New menu, seeding new files from a bundled template. */
public final class CreateRefalFileAction extends CreateFileFromTemplateAction {

    @Override
    protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory directory,
                               @NotNull CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle("New Refal File")
                .addKind("Refal file", RefalIcons.FILE, "Refal File");
    }

    @Override
    protected String getActionName(PsiDirectory directory, @NotNull String newName, String templateName) {
        return "Create Refal File";
    }
}
