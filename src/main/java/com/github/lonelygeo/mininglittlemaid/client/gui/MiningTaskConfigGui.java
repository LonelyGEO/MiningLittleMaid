package com.github.lonelygeo.mininglittlemaid.client.gui;

import com.github.lonelygeo.mininglittlemaid.inventory.container.MiningTaskConfigContainer;
import com.github.lonelygeo.mininglittlemaid.task.MaidMineBreakTask;
import com.github.lonelygeo.mininglittlemaid.task.OreToggleManager;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.task.MaidTaskConfigGui;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.MaidConfigButton;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class MiningTaskConfigGui extends MaidTaskConfigGui<MiningTaskConfigContainer> {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath("touhou_little_maid",
                    "textures/gui/attack_task_config.png");
    private static final int ROW_HEIGHT = 20;
    private static final int SCROLL_AREA_TITLE_Y = 76;
    private static final int SCROLL_AREA_START_Y = 96;
    private static final int SCROLL_AREA_HEIGHT = 80;
    private static final int BUTTON_X = 86;

    private final List<OreToggleManager.OreGroup> oreGroups = new ArrayList<>();
    private final List<MaidConfigButton> oreButtons = new ArrayList<>();
    private int scrollOffset;
    private int maxScroll;
    private int visibleRows;
    private boolean needsRefresh;

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
        oreGroups.clear();
        oreGroups.addAll(OreToggleManager.getOreGroups());

        visibleRows = SCROLL_AREA_HEIGHT / ROW_HEIGHT;
        maxScroll = Math.max(0, oreGroups.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        // 聊天通知开关（现有）
        boolean enabled = MaidMineBreakTask.isChatNotifyEnabled(maid);
        Component label = Component.translatable("gui.mininglittlemaid.chat_notify");
        Component value = Component.translatable(enabled
                ? "gui.mininglittlemaid.option.on"
                : "gui.mininglittlemaid.option.off");
        MaidConfigButton toggleBtn = new MaidConfigButton(
                leftPos + BUTTON_X, topPos + 52, label, value,
                btn -> {
                    MaidMineBreakTask.toggleChatNotify(maid);
                    boolean newState = MaidMineBreakTask.isChatNotifyEnabled(maid);
                    btn.setValue(Component.translatable(newState
                            ? "gui.mininglittlemaid.option.on"
                            : "gui.mininglittlemaid.option.off"));
                });
        addRenderableWidget(toggleBtn);

        // 矿石开关列表
        createOreButtons(maid);
    }

    private void createOreButtons(EntityMaid maid) {
        oreButtons.clear();
        int startY = topPos + SCROLL_AREA_START_Y - scrollOffset * ROW_HEIGHT;

        for (int i = 0; i < oreGroups.size(); i++) {
            OreToggleManager.OreGroup group = oreGroups.get(i);
            int y = startY + i * ROW_HEIGHT;

            boolean isEnabled = OreToggleManager.isOreGroupEnabled(maid, group.groupKey());
            Component btnLabel = group.representativeBlock().getName();
            Component btnValue = Component.translatable(isEnabled
                    ? "gui.mininglittlemaid.option.on"
                    : "gui.mininglittlemaid.option.off");

            MaidConfigButton btn = new MaidConfigButton(
                    leftPos + BUTTON_X, y, btnLabel, btnValue,
                    b -> {
                        OreToggleManager.toggleOreGroup(maid, group.groupKey());
                        boolean newState = OreToggleManager.isOreGroupEnabled(maid, group.groupKey());
                        b.setValue(Component.translatable(newState
                                ? "gui.mininglittlemaid.option.on"
                                : "gui.mininglittlemaid.option.off"));
                    });
            oreButtons.add(btn);
            addRenderableWidget(btn);
        }
        updateButtonVisibility();
    }

    private void refreshOreButtons() {
        EntityMaid maid = getMaid();
        int startY = topPos + SCROLL_AREA_START_Y - scrollOffset * ROW_HEIGHT;

        for (int i = 0; i < oreButtons.size(); i++) {
            int y = startY + i * ROW_HEIGHT;
            oreButtons.get(i).setY(y);
        }
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        int scrollAreaTop = topPos + SCROLL_AREA_START_Y;
        int scrollAreaBottom = scrollAreaTop + SCROLL_AREA_HEIGHT;

        for (int i = 0; i < oreButtons.size(); i++) {
            MaidConfigButton btn = oreButtons.get(i);
            int btnBottom = btn.getY() + ROW_HEIGHT;
            btn.visible = btn.getY() >= scrollAreaTop && btnBottom <= scrollAreaBottom;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int scrollAreaTop = topPos + SCROLL_AREA_START_Y;
        int scrollAreaBottom = scrollAreaTop + SCROLL_AREA_HEIGHT;

        if (mouseX >= leftPos + 80 && mouseX <= leftPos + 80 + imageWidth
                && mouseY >= scrollAreaTop && mouseY <= scrollAreaBottom) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - Math.signum(scrollY)));
            refreshOreButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderAddition(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Component title = Component.translatable("gui.mininglittlemaid.ore_toggles");
        graphics.drawString(font, title, leftPos + BUTTON_X, topPos + SCROLL_AREA_TITLE_Y, 0x404040, false);

        if (maxScroll > 0) {
            int barWidth = 4;
            int scrollAreaTopPixel = topPos + SCROLL_AREA_START_Y;
            int thumbHeight = Math.max(8, visibleRows * SCROLL_AREA_HEIGHT / oreGroups.size());
            int trackHeight = SCROLL_AREA_HEIGHT - thumbHeight;
            int thumbY = trackHeight == 0 ? scrollAreaTopPixel
                    : scrollAreaTopPixel + scrollOffset * trackHeight / maxScroll;
            int barX = leftPos + BUTTON_X + 110;
            graphics.fill(barX, scrollAreaTopPixel, barX + barWidth, scrollAreaTopPixel + SCROLL_AREA_HEIGHT, 0x33FFFFFF);
            graphics.fill(barX, thumbY, barX + barWidth, thumbY + thumbHeight, 0x99FFFFFF);
        }
    }
}
