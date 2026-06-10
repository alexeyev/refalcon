package com.github.refal5lambda.run;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Stores the settings for compiling and running a single Refal-5 Lambda program. */
public final class RefalRunConfiguration extends RunConfigurationBase<Object> {

    /** Empty means "auto-detect" (PATH + standard install locations); see RefalCommandLineState. */
    private String compilerPath = "";
    private String compilerOptions = "";
    private String sourceFile = "";
    private boolean runAfterCompile = true;
    private String outputExecutable = "";
    private String programArguments = "";
    private String workingDirectory = "";

    public RefalRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new RefalSettingsEditor(getProject());
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new RefalCommandLineState(environment, this);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (StringUtil.isEmptyOrSpaces(sourceFile)) {
            throw new RuntimeConfigurationError("Specify the Refal source file (.ref).");
        }
        String c = compilerPath == null ? "" : compilerPath.trim();
        if ((c.isEmpty() || "rlc".equals(c)) && RefalCompilerLocator.detect() == null) {
            throw new RuntimeConfigurationWarning(
                    "rlc was not found on PATH \u2014 install it (https://github.com/bmstu-iu9/refal-5-lambda) "
                    + "or set its full path here.");
        }
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        compilerPath = orDefault(JDOMExternalizerUtil.readField(element, "compilerPath"), "");
        compilerOptions = orDefault(JDOMExternalizerUtil.readField(element, "compilerOptions"), "");
        sourceFile = orDefault(JDOMExternalizerUtil.readField(element, "sourceFile"), "");
        runAfterCompile = !"false".equals(JDOMExternalizerUtil.readField(element, "runAfterCompile"));
        outputExecutable = orDefault(JDOMExternalizerUtil.readField(element, "outputExecutable"), "");
        programArguments = orDefault(JDOMExternalizerUtil.readField(element, "programArguments"), "");
        workingDirectory = orDefault(JDOMExternalizerUtil.readField(element, "workingDirectory"), "");
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        JDOMExternalizerUtil.writeField(element, "compilerPath", compilerPath);
        JDOMExternalizerUtil.writeField(element, "compilerOptions", compilerOptions);
        JDOMExternalizerUtil.writeField(element, "sourceFile", sourceFile);
        JDOMExternalizerUtil.writeField(element, "runAfterCompile", Boolean.toString(runAfterCompile));
        JDOMExternalizerUtil.writeField(element, "outputExecutable", outputExecutable);
        JDOMExternalizerUtil.writeField(element, "programArguments", programArguments);
        JDOMExternalizerUtil.writeField(element, "workingDirectory", workingDirectory);
    }

    private static String orDefault(String value, String fallback) {
        return value == null ? fallback : value;
    }

    public String getCompilerPath() { return compilerPath; }
    public void setCompilerPath(String v) { compilerPath = v; }

    public String getCompilerOptions() { return compilerOptions; }
    public void setCompilerOptions(String v) { compilerOptions = v; }

    public String getSourceFile() { return sourceFile; }
    public void setSourceFile(String v) { sourceFile = v; }

    public boolean isRunAfterCompile() { return runAfterCompile; }
    public void setRunAfterCompile(boolean v) { runAfterCompile = v; }

    public String getOutputExecutable() { return outputExecutable; }
    public void setOutputExecutable(String v) { outputExecutable = v; }

    public String getProgramArguments() { return programArguments; }
    public void setProgramArguments(String v) { programArguments = v; }

    public String getWorkingDirectory() { return workingDirectory; }
    public void setWorkingDirectory(String v) { workingDirectory = v; }
}
