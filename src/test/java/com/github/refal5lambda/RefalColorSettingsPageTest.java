package com.github.refal5lambda;

import com.intellij.openapi.options.colors.AttributesDescriptor;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** The color settings page must offer descriptors and demo text and a highlighter. */
public class RefalColorSettingsPageTest {

    @Test
    public void pageIsWellFormed() {
        RefalColorSettingsPage page = new RefalColorSettingsPage();
        assertNotNull(page.getHighlighter());
        AttributesDescriptor[] descriptors = page.getAttributeDescriptors();
        assertTrue("expected color descriptors", descriptors.length > 0);
        for (AttributesDescriptor d : descriptors) {
            assertNotNull(d.getDisplayName());
            assertNotNull(d.getKey());
        }
        assertNotNull(page.getDemoText());
        assertFalse(page.getDemoText().isEmpty());
        assertNotNull(page.getDisplayName());
    }
}
