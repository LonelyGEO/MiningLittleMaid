package com.github.lonelygeo.mininglittlemaid.client.gui;

import com.github.lonelygeo.mininglittlemaid.inventory.container.MiningTaskConfigContainer;
import com.github.lonelygeo.mininglittlemaid.task.MaidMineBreakTask;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.task.MaidTaskConfigGui;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MiningTaskConfigGui extends MaidTaskConfigGui<MiningTaskConfigContainer> {

    public MiningTaskConfigGui(MiningTaskConfigContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected void initAdditionWidgets() {
        EntityMaid maid = getMaid();
        boolean enabled = MaidMineBreakTask.isChatNotifyEnabled(maid);
        Component btnText = Component.translatable("gui.mining_little_maid.chat_notify")
                .append(": ")
                .append(Component.translatable(enabled
                        ? "gui.mining_little_maid.option.on"
                        : "gui.mining_little_maid.option.off"));
        Button toggleBtn = Button.builder(btnText, btn -> {
                    MaidMineBreakTask.toggleChatNotify(maid);
                    boolean newState = MaidMineBreakTask.isChatNotifyEnabled(maid);
                    btn.setMessage(Component.translatable("gui.mining_little_maid.chat_notify")
                            .append(": ")
                            .append(Component.translatable(newState
                                    ? "gui.mining_little_maid.option.on"
                                    : "gui.mining_little_maid.option.off")));
                })
                .pos(leftPos + 86, topPos + 52)
                .size(164, 20)
                .build();
        addRenderableWidget(toggleBtn);
    }

    @Override
    protected void renderAddition(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    }
}
