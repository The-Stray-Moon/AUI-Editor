package cc.sighs.auieditor;

import cc.sighs.auieditor.editor.ore.OreEditorCommands;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public final class AUIEditorFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> OreEditorCommands.register(dispatcher));
    }
}
