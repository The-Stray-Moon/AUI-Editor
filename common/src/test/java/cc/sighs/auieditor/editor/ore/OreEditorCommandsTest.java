package cc.sighs.auieditor.editor.ore;

import com.mojang.brigadier.CommandDispatcher;
import com.sighs.apricityui.parser.HTML;
import com.sighs.apricityui.ui.FilePicker;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OreEditorCommandsTest {
    @Test
    void commandOpensEditorAndProtectsDirtyProjectBeforeFilePicking() throws Exception {
        OreEditor.getSession().setDirty(false);
        OreEditor.close();
        HTML.putTemple(OreEditorController.PATH, Files.readString(Path.of(System.getProperty("auieditor.resources"),
                "assets/apricityui/apricity/editor/ore/ore-editor.html")));
        CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();
        OreEditorCommands.register(dispatcher);
        try {
            assertEquals(1, dispatcher.execute("auieditor", new Object()));
            assertTrue(OreEditor.isOpen());
            OreEditor.getSession().setDirty(true);
            assertEquals(0, dispatcher.execute("auieditor open", new Object()));
            assertTrue(OreEditor.isOpen());
            assertTrue(OreEditor.getSession().dirty());
            assertFalse(FilePicker.isOpen());
        } finally {
            OreEditor.getSession().setDirty(false);
            OreEditor.close();
        }
    }
}
