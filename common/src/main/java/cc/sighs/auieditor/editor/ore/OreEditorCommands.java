package cc.sighs.auieditor.editor.ore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sighs.apricityui.ui.FilePicker;
import com.sighs.apricityui.ui.ToastManager;

/** Client-only entry point shared by every loader. */
public final class OreEditorCommands {
    private OreEditorCommands() {
    }

    public static <S> void register(CommandDispatcher<S> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<S>literal("auieditor")
                .executes(context -> OreEditor.open() ? 1 : 0)
                .then(LiteralArgumentBuilder.<S>literal("open").executes(context -> {
                    if (OreEditor.isOpen() && OreEditor.getSession().dirty()) {
                        ToastManager.showTranslation("ore_editor.auieditor.unsaved");
                        return 0;
                    }
                    FilePicker.pick(FilePicker.Options.htmlTranslation("ore_editor.auieditor.select_html", false))
                            .thenAccept(selection -> selection.ifPresent(file -> {
                                if (!OreEditor.openHtml(file.localPath())) {
                                    ToastManager.showTranslation("ore_editor.auieditor.open_failed");
                                }
                            }));
                    return 1;
                })));
    }
}
