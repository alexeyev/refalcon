package com.github.refal5lambda;

import com.github.refal5lambda.psi.RefalFunction;
import com.github.refal5lambda.r2.Refal2Function;
import com.intellij.psi.search.PsiTodoSearchHelper;
import com.intellij.psi.search.TodoItem;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * The dialect-agnostic editor aids, tested through real behavior rather than wiring shape:
 * TODO markers are actually discovered inside Refal comments (and ignored elsewhere), and
 * breadcrumbs label the enclosing function in both dialects.
 */
public class RefalEditorFeaturesTest extends BasePlatformTestCase {

    public void testTodoMarkersAreFoundInsideRefalCommentsOnly() {
        myFixture.configureByText("a.ref",
                "* TODO: handle the empty case\n"
                + "$ENTRY Go { = <Prout 'TODO is only text here, not a comment'>; }\n");
        TodoItem[] todos = PsiTodoSearchHelper.getInstance(getProject())
                .findTodoItems(myFixture.getFile());
        // Exactly one: the comment marker. The 'TODO' inside the string literal is not scanned.
        assertEquals(1, todos.length);
    }

    public void testTodoInRefal2CommentIsFound() {
        myFixture.configureByText("m.ref",
                "m start\n"
                + "* FIXME: case-insensitive names\n"
                + " entry go\n"
                + "go = <Print>\n"
                + " end\n");
        assertEquals("Refal2", myFixture.getFile().getLanguage().getID());
        TodoItem[] todos = PsiTodoSearchHelper.getInstance(getProject())
                .findTodoItems(myFixture.getFile());
        assertTrue("expected the FIXME marker to be found", todos.length >= 1);
    }

    public void testBreadcrumbsLabelLambdaFunctionsByName() {
        RefalBreadcrumbsProvider provider = new RefalBreadcrumbsProvider();
        myFixture.configureByText("a.ref", "$ENTRY MyFunc { = ; }\n");
        RefalFunction fn = PsiTreeUtil.findChildOfType(myFixture.getFile(), RefalFunction.class);
        assertNotNull(fn);
        assertTrue(provider.acceptElement(fn));
        assertEquals("MyFunc", provider.getElementInfo(fn));
        assertEquals(2, provider.getLanguages().length);
    }

    public void testBreadcrumbsLabelRefal2FunctionsByName() {
        RefalBreadcrumbsProvider provider = new RefalBreadcrumbsProvider();
        myFixture.configureByText("m.ref",
                "m start\n entry go\ngo = <Print>\nhelper = '1'\n end\n");
        assertEquals("Refal2", myFixture.getFile().getLanguage().getID());
        Refal2Function fn = PsiTreeUtil.findChildOfType(myFixture.getFile(), Refal2Function.class);
        assertNotNull(fn);
        assertTrue(provider.acceptElement(fn));
        assertNotNull(provider.getElementInfo(fn));
    }

    public void testTodoBuilderIgnoresNonRefalFiles() {
        // Good citizenship: our index-pattern builder must not claim files of other languages.
        RefalIndexPatternBuilder builder = new RefalIndexPatternBuilder();
        myFixture.configureByText("a.txt", "TODO: not ours\n");
        assertNull(builder.getIndexingLexer(myFixture.getFile()));
        assertNull(builder.getCommentTokenSet(myFixture.getFile()));
    }
}
