package cc.sighs.auieditor;

import cc.sighs.auieditor.editor.ore.OreEditorCommands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = "auieditor", value = Dist.CLIENT)
public final class AUIEditorClientCommands {
    private AUIEditorClientCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        OreEditorCommands.register(event.getDispatcher());
    }
}
