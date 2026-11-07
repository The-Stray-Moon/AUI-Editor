package cc.sighs.auieditor.editor.ore;

import com.sighs.apricityui.init.Document;
import com.sighs.apricityui.init.Element;
import com.sighs.apricityui.parser.HTML;
import cc.sighs.auieditor.editor.ore.OreEditorTranslations;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OreEditorCloseTest {
    private static final Path TEMPLATE = Path.of(System.getProperty("auieditor.resources"), "assets/apricityui/apricity/editor/ore/ore-editor.html");

    @Test
    void shellBindsAccessibilityLabelsFromTranslationKeys() throws Exception {
        OreEditorController controller = OreEditorController.INSTANCE;
        controller.close();
        HTML.putTemple(OreEditorController.PATH, Files.readString(TEMPLATE));
        assertTrue(controller.open());
        try {
            Element tabs = controller.getDocument().querySelector(".editor-tabs");
            assertNotNull(tabs);
            assertTrue("ore_editor.auieditor.accessibility.tabs".equals(tabs.getAttribute("data-aria-label-key")));
            assertEquals(OreEditorTranslations.translate("ore_editor.auieditor.accessibility.tabs"), tabs.getAttribute("aria-label"));
            assertFalse("ore_editor.auieditor.accessibility.tabs".equals(tabs.getAttribute("aria-label")));
            assertFalse("ore-editor-tabs".equals(tabs.getAttribute("aria-label")));
        } finally {
            controller.close();
        }
    }

    @Test
    void dirtyProjectRequiresConfirmationBeforeClosing() throws Exception {
        OreEditorController controller = OreEditorController.INSTANCE;
        controller.close();
        HTML.putTemple(OreEditorController.PATH, Files.readString(TEMPLATE));
        assertTrue(controller.open());
        try {
            controller.getSession().setDirty(true);
            controller.close();

            Document editor = controller.getDocument();
            assertNotNull(editor);
            Element dialog = editor.querySelector("[data-ore-editor-ui=\"unsaved-changes-dialog\"]");
            assertNotNull(dialog);
            assertNotNull(dialog.querySelector("TRANSLATION"));

            dialog.querySelector(".button-danger").click();
            assertFalse(controller.isOpen());
        } finally {
            if (controller.isOpen()) {
                controller.getSession().setDirty(false);
                controller.close();
            }
        }
    }

    @Test
    void dirtyProjectRequiresConfirmationBeforeLoadingSavedProject() throws Exception {
        OreEditorController controller = OreEditorController.INSTANCE;
        controller.close();
        HTML.putTemple(OreEditorController.PATH, Files.readString(TEMPLATE));
        assertTrue(controller.open());
        try {
            Document editor = controller.getDocument();
            controller.getSession().setDirty(true);
            Element load = editor.querySelector("#loadButton");
            assertNotNull(load);
            load.click();

            Element dialog = editor.querySelector("[data-ore-editor-ui=\"unsaved-changes-dialog\"]");
            assertNotNull(dialog);
            assertTrue(dialog.querySelector("TRANSLATION").getTextContent()
                    .equals("ore_editor.auieditor.dialog.load.title"));
        } finally {
            if (controller.isOpen()) {
                controller.getSession().setDirty(false);
                controller.close();
            }
        }
    }
}
