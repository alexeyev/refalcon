package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalTypes;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Instant, compiler-free error highlighting computed from the PSI as you type (no {@code rlc}
 * required):
 * <ul>
 *   <li><b>Unresolved function</b> — a call whose name is neither a known built-in / standard-library
 *       function, nor defined anywhere in the project, nor declared in a directive.</li>
 *   <li><b>Unresolved variable</b> — a variable used on the result side of a sentence that is not
 *       bound by the sentence's pattern, an enclosing sentence's pattern, or a result-side match.</li>
 * </ul>
 *
 * <p>The idea of detecting these from the parse tree (rather than from the compiler) is borrowed
 * from the official plugin <a href="https://github.com/bmstu-iu9/RefalFiveLambdaPlugin">
 * bmstu-iu9/RefalFiveLambdaPlugin</a>. The implementation here is independent and built on this
 * plugin's own grammar, and it is intentionally conservative so that valid code is not flagged.
 * It complements {@code RefalExternalAnnotator}, which reports the full set of real compiler errors
 * when {@code rlc} is available.
 */
public final class RefalAnnotator implements Annotator {

    private static final Key<FileNames> NAMES = Key.create("refal.file.names");

    /** Function names defined in the file, and names declared in directives. */
    private static final class FileNames {
        final Set<String> defined;
        final Set<String> declared;
        FileNames(Set<String> defined, Set<String> declared) {
            this.defined = defined;
            this.declared = declared;
        }
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        IElementType type = PsiUtilCore.getElementType(element);
        if (type == RefalTypes.CALL) {
            checkCall(element, holder);
        } else if (type == RefalTypes.SENTENCE) {
            checkSentence(element, holder);
        }
    }

    // ----- unresolved function ----------------------------------------------

    private void checkCall(PsiElement call, AnnotationHolder holder) {
        String name = call.getText();
        if (name.isEmpty() || RefalBuiltins.isKnownFunction(name)) return;
        // A call is "known" if it is a built-in, defined in this file, or declared in a directive.
        // Cross-unit calls require a $EXTERN declaration in Refal, so this needs no (costly)
        // cross-file resolution — keeping the per-keystroke highlighting pass fast.
        FileNames names = names(call.getContainingFile(), holder);
        if (names.defined.contains(name) || names.declared.contains(name)) return;
        holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved function: " + name)
                .range(call).create();
    }

    /** Defined-function names and directive-declared names, computed in one pass and cached. */
    private FileNames names(PsiFile file, AnnotationHolder holder) {
        FileNames cached = holder.getCurrentAnnotationSession().getUserData(NAMES);
        if (cached != null) return cached;
        Set<String> defined = new HashSet<>();
        Set<String> declared = new HashSet<>();
        if (file != null) collectNames(file, defined, declared, false);
        cached = new FileNames(defined, declared);
        holder.getCurrentAnnotationSession().putUserData(NAMES, cached);
        return cached;
    }

    private void collectNames(PsiElement node, Set<String> defined, Set<String> declared,
                              boolean insideDirective) {
        boolean inDirective = insideDirective || PsiUtilCore.getElementType(node) == RefalTypes.DIRECTIVE;
        for (PsiElement c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getFirstChild() == null) {
                IElementType t = PsiUtilCore.getElementType(c);
                if (t == RefalTokenTypes.FUNCTION_DEFINITION) {
                    defined.add(c.getText());
                    if (inDirective) declared.add(c.getText());
                } else if (inDirective
                        && (t == RefalTokenTypes.IDENTIFIER || t == RefalTokenTypes.FUNCTION_CALL)) {
                    declared.add(c.getText());
                }
            } else {
                collectNames(c, defined, declared, inDirective);
            }
        }
    }

    // ----- unresolved variable ----------------------------------------------

    private void checkSentence(PsiElement sentence, AnnotationHolder holder) {
        PsiElement result = childOfType(sentence, RefalTypes.RESULT);
        if (result == null) return;                  // no '=', nothing to verify

        Set<String> bound = new HashSet<>();
        PsiElement pattern = childOfType(sentence, RefalTypes.PATTERN);
        if (pattern != null) collectVariables(pattern, bound);
        // Closures: variables bound by enclosing sentences' patterns are visible here too.
        for (PsiElement a = sentence.getParent(); a != null && !(a instanceof PsiFile); a = a.getParent()) {
            if (PsiUtilCore.getElementType(a) == RefalTypes.SENTENCE) {
                PsiElement p = childOfType(a, RefalTypes.PATTERN);
                if (p != null) collectVariables(p, bound);
            }
        }

        List<PsiElement> uses = new ArrayList<>();
        scanResult(result, new boolean[]{false}, bound, uses);   // result-side matches add to `bound`

        for (PsiElement use : uses) {
            if (!bound.contains(use.getText())) {
                holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved variable: " + use.getText())
                        .range(use).create();
            }
        }
    }

    /**
     * Ordered walk of the result: skips nested blocks (their own scope) and threads a flag for
     * "inside a {@code : pattern} match", whose variables are bindings rather than uses.
     */
    private void scanResult(PsiElement node, boolean[] inColonPattern,
                            Set<String> bound, List<PsiElement> uses) {
        for (PsiElement c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
            IElementType t = PsiUtilCore.getElementType(c);
            if (t == RefalTypes.BLOCK) continue;                 // nested scope
            if (c.getFirstChild() == null) {                     // leaf
                if (t == RefalTokenTypes.COLON) {
                    inColonPattern[0] = true;
                } else if (t == RefalTokenTypes.COMMA || t == RefalTokenTypes.EQ
                        || t == RefalTokenTypes.SEMICOLON) {
                    inColonPattern[0] = false;
                } else if (isVariable(t)) {
                    if (inColonPattern[0]) bound.add(c.getText());
                    else uses.add(c);
                }
            } else {
                scanResult(c, inColonPattern, bound, uses);
            }
        }
    }

    private static void collectVariables(PsiElement node, Set<String> out) {
        if (node.getFirstChild() == null) {
            if (isVariable(PsiUtilCore.getElementType(node))) out.add(node.getText());
            return;
        }
        for (PsiElement c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
            collectVariables(c, out);
        }
    }

    private static boolean isVariable(IElementType t) {
        return t == RefalTokenTypes.S_VARIABLE || t == RefalTokenTypes.T_VARIABLE
                || t == RefalTokenTypes.E_VARIABLE;
    }

    private static PsiElement childOfType(PsiElement parent, IElementType type) {
        for (PsiElement c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (PsiUtilCore.getElementType(c) == type) return c;
        }
        return null;
    }
}
