package com.github.refal5lambda;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Map;

public final class RefalColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Line comment", RefalSyntaxHighlighter.LINE_COMMENT),
            new AttributesDescriptor("Block comment", RefalSyntaxHighlighter.BLOCK_COMMENT),
            new AttributesDescriptor("Embedded native block", RefalSyntaxHighlighter.NATIVE_BLOCK),
            new AttributesDescriptor("String", RefalSyntaxHighlighter.STRING),
            new AttributesDescriptor("String escape//Valid", RefalSyntaxHighlighter.STRING_ESCAPE_VALID),
            new AttributesDescriptor("String escape//Invalid", RefalSyntaxHighlighter.STRING_ESCAPE_INVALID),
            new AttributesDescriptor("Number", RefalSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("Keyword (directive)", RefalSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("Unknown directive", RefalSyntaxHighlighter.BAD_DIRECTIVE),
            new AttributesDescriptor("Variable//s-variable", RefalSyntaxHighlighter.S_VARIABLE),
            new AttributesDescriptor("Variable//t-variable", RefalSyntaxHighlighter.T_VARIABLE),
            new AttributesDescriptor("Variable//e-variable", RefalSyntaxHighlighter.E_VARIABLE),
            new AttributesDescriptor("Function definition", RefalSyntaxHighlighter.FUNCTION_DEFINITION),
            new AttributesDescriptor("Function call", RefalSyntaxHighlighter.FUNCTION_CALL),
            new AttributesDescriptor("Identifier (symbol)", RefalSyntaxHighlighter.IDENTIFIER),
            new AttributesDescriptor("Braces { }", RefalSyntaxHighlighter.BRACES),
            new AttributesDescriptor("Parentheses ( )", RefalSyntaxHighlighter.PARENTHESES),
            new AttributesDescriptor("Activation brackets < >", RefalSyntaxHighlighter.ANGLES),
            new AttributesDescriptor("Square brackets [ ]", RefalSyntaxHighlighter.BRACKETS),
            new AttributesDescriptor("Semicolon", RefalSyntaxHighlighter.SEMICOLON),
            new AttributesDescriptor("Comma", RefalSyntaxHighlighter.COMMA),
            new AttributesDescriptor("Operator ( = : )", RefalSyntaxHighlighter.OPERATOR),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return RefalIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new RefalSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "* Factorial in Refal-5\u03bb\n"
                + "/* a block comment */\n"
                + "$ENTRY Go {\n"
                + "  = <Prout <Fact 5>>;\n"
                + "}\n"
                + "\n"
                + "Fact {\n"
                + "  0   = 1;\n"
                + "  s.N = <Mul s.N <Fact <Sub s.N 1>>>;\n"
                + "}\n"
                + "\n"
                + "$EXTERN Mul, Sub;\n"
                + "\n"
                + "Greet {\n"
                + "  e.Name = 'Hello, ' e.Name '!\\n' '\\x21' '\\q';\n"
                + "}\n";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Refal-5\u03bb";
    }
}
