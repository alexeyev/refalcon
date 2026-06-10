package com.github.refal5lambda.run;

import com.github.refal5lambda.RefalFileType;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Lets you create/run a configuration straight from a .ref file (right-click or Ctrl+Shift+F10). */
public final class RefalRunConfigurationProducer extends LazyRunConfigurationProducer<RefalRunConfiguration> {

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return ConfigurationTypeUtil.findConfigurationType(RefalRunConfigurationType.class)
                .getConfigurationFactories()[0];
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull RefalRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        VirtualFile file = refalFile(context);
        if (file == null) return false;
        configuration.setSourceFile(file.getPath());
        if (file.getParent() != null) {
            configuration.setWorkingDirectory(file.getParent().getPath());
        }
        configuration.setName(file.getNameWithoutExtension());
        // Auto-detect the compiler so a freshly created config runs without manual setup.
        if (StringUtil.isEmptyOrSpaces(configuration.getCompilerPath())
                || "rlc".equals(configuration.getCompilerPath())) {
            String detected = RefalCompilerLocator.detect();
            if (detected != null) configuration.setCompilerPath(detected);
        }
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull RefalRunConfiguration configuration,
                                              @NotNull ConfigurationContext context) {
        VirtualFile file = refalFile(context);
        return file != null && file.getPath().equals(configuration.getSourceFile());
    }

    @Nullable
    private static VirtualFile refalFile(@NotNull ConfigurationContext context) {
        PsiElement location = context.getPsiLocation();
        if (location == null) return null;
        PsiFile psiFile = location.getContainingFile();
        if (psiFile == null) return null;
        VirtualFile vf = psiFile.getVirtualFile();
        if (vf == null) return null;
        return vf.getFileType() == RefalFileType.INSTANCE ? vf : null;
    }
}
