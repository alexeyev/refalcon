package com.github.refal5lambda.run;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;

/** The form shown in Run/Debug Configurations for a Refal-5 Lambda run configuration. */
public final class RefalSettingsEditor extends SettingsEditor<RefalRunConfiguration> {

    private final TextFieldWithBrowseButton compilerPath = new TextFieldWithBrowseButton();
    private final RawCommandLineEditor compilerOptions = new RawCommandLineEditor();
    private final TextFieldWithBrowseButton sourceFile = new TextFieldWithBrowseButton();
    private final JBCheckBox runAfterCompile = new JBCheckBox("Run the compiled executable after a successful compile");
    private final JBCheckBox useRlmake = new JBCheckBox("Build with rlmake (multi-file: follows *$FROM dependencies)");
    private final TextFieldWithBrowseButton outputExecutable = new TextFieldWithBrowseButton();
    private final RawCommandLineEditor programArguments = new RawCommandLineEditor();
    private final TextFieldWithBrowseButton workingDirectory = new TextFieldWithBrowseButton();
    private final JPanel panel;

    public RefalSettingsEditor(Project project) {
        compilerPath.addBrowseFolderListener(project,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
                        .withTitle("Refal Compiler").withDescription("Select the rlc executable"));
        if (compilerPath.getTextField() instanceof JBTextField hint) {
            hint.getEmptyText().setText("Auto-detect (rlc/rlmake on PATH or in a standard install location)");
        }
        sourceFile.addBrowseFolderListener(project,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
                        .withTitle("Refal Source File").withDescription("Select a .ref file"));
        outputExecutable.addBrowseFolderListener(project,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
                        .withTitle("Compiled Executable").withDescription("Select the compiled program"));
        workingDirectory.addBrowseFolderListener(project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
                        .withTitle("Working Directory").withDescription("Select the working directory"));

        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Refal compiler (rlc):", compilerPath)
                .addLabeledComponent("Compiler options:", compilerOptions)
                .addLabeledComponent("Refal file:", sourceFile)
                .addComponent(useRlmake)
                .addComponent(runAfterCompile)
                .addLabeledComponent("Output executable:", outputExecutable)
                .addLabeledComponent("Program arguments:", programArguments)
                .addLabeledComponent("Working directory:", workingDirectory)
                .getPanel();
    }

    @Override
    protected void resetEditorFrom(@NotNull RefalRunConfiguration c) {
        compilerPath.setText(c.getCompilerPath());
        compilerOptions.setText(c.getCompilerOptions());
        sourceFile.setText(c.getSourceFile());
        runAfterCompile.setSelected(c.isRunAfterCompile());
        useRlmake.setSelected(c.isUseRlmake());
        outputExecutable.setText(c.getOutputExecutable());
        programArguments.setText(c.getProgramArguments());
        workingDirectory.setText(c.getWorkingDirectory());
    }

    @Override
    protected void applyEditorTo(@NotNull RefalRunConfiguration c) {
        c.setCompilerPath(compilerPath.getText());
        c.setCompilerOptions(compilerOptions.getText());
        c.setSourceFile(sourceFile.getText());
        c.setRunAfterCompile(runAfterCompile.isSelected());
        c.setUseRlmake(useRlmake.isSelected());
        c.setOutputExecutable(outputExecutable.getText());
        c.setProgramArguments(programArguments.getText());
        c.setWorkingDirectory(workingDirectory.getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return panel;
    }
}
