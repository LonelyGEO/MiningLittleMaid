package com.github.lonelygeo.mininglittlemaid.client.gui;

import com.github.lonelygeo.mininglittlemaid.inventory.container.MiningTaskConfigContainer;
import com.github.lonelygeo.mininglittlemaid.task.MaidMineBreakTask;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.task.MaidTaskConfigGui;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.MaidConfigButton;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MiningTaskConfigGui extends MaidTaskConfigGui<MiningTaskConfigContainer> {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath("touhou_little_maid",
                    "textures/gui/attack_task_config.png");

    public MiningTaskConfigGui(MiningTaskConfigContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);
        graphics.blit(BG, leftPos + 80, topPos + 28, 0, 0, imageWidth, 137);
    }

    @Override
    protected void initAdditionWidgets() {
        EntityMaid maid = getMaid();
        boolean enabled = MaidMineBreakTask.isChatNotifyEnabled(maid);
        Component label = Component.translatable("gui.mininglittlemaid.chat_notify");
        Component value = Component.translatable(enabled
                ? "gui.mininglittlemaid.option.on"
                : "gui.mininglittlemaid.option.off");
        MaidConfigButton toggleBtn = new MaidConfigButton(
                leftPos + 86, topPos + 52, label, value,
                btn -> {
                    MaidMineBreakTask.toggleChatNotify(maid);
                    boolean newState = MaidMineBreakTask.isChatNotifyEnabled(maid);
                    btn.setValue(Component.translatable(newState
                            ? "gui.mininglittlemaid.option.on"
                            : "gui.mininglittlemaid.option.off"));
                });
        addRenderableWidget(toggleBtn);
    }

    @Override
    protected void renderAddition(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    }
}
