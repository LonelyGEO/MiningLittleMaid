package com.github.lonelygeo.mininglittlemaid.event;

import com.github.lonelygeo.mininglittlemaid.MiningLittleMaid;
import com.github.lonelygeo.mininglittlemaid.network.MiningChatNotifyToggleMessage;
import com.github.lonelygeo.mininglittlemaid.task.MaidMineBreakTask;
import com.github.tartaricacid.touhoulittlemaid.api.event.client.MaidContainerGuiEvent;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.config.MaidConfigContainerGui;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = MiningLittleMaid.MOD_ID, value = Dist.CLIENT)
public class ClientGuiEventHandler {
    private static final Component CHAT_LABEL = Component.translatable(
            "gui.mining_little_maid.chat_notify");

    @SubscribeEvent
    public static void onInitMaidGui(MaidContainerGuiEvent.Init event) {
        if (!(event.getGui() instanceof MaidConfigContainerGui)) {
            return;
        }
        EntityMaid maid = (EntityMaid) event.getGui().getMaid();
        int x = event.getLeftPos() + 86;
        int y = event.getTopPos() + 158;
        Checkbox checkBox = Checkbox.builder(CHAT_LABEL, Minecraft.getInstance().font)
                .pos(x, y)
                .selected(MaidMineBreakTask.isChatNotifyEnabled(maid))
                .onValueChange((box, value) -> PacketDistributor.sendToServer(
                        new MiningChatNotifyToggleMessage(maid.getId())))
                .build();
        event.addButton("mining_chat_notify", checkBox);
    }
}
