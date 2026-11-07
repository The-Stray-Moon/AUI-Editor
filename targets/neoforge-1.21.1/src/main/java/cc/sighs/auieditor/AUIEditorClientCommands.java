package cc.sighs.auieditor;

import cc.sighs.auieditor.editor.ore.OreEditorCommands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = "auieditor", value = Dist.CLIENT)
public final class AUIEditorClientCommands {
    private AUIEditorClientCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        OreEditorCommands.register(event.getDispatcher());
    }
}
