package com.github.lonelygeo.mininglittlemaid.client.gui;

import com.github.lonelygeo.mininglittlemaid.inventory.container.MiningTaskConfigContainer;
import com.github.lonelygeo.mininglittlemaid.task.MaidMineBreakTask;
import com.github.lonelygeo.mininglittlemaid.task.OreToggleManager;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.task.MaidTaskConfigGui;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.MaidConfigButton;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class MiningTaskConfigGui extends MaidTaskConfigGui<MiningTaskConfigContainer> {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath("touhou_little_maid",
                    "textures/gui/attack_task_config.png");
    private static final int BG_HEIGHT = 137;
    private static final int BG_OFFSET_Y = 28;
    private static final int PANEL_X_OFFSET = 80;
    private static final int PANEL_Y_OFFSET = 28;
    private static final int TITLE_X_OFFSET = 88;
    private static final int TITLE_Y_OFFSET = 38;
    private static final int CHAT_BUTTON_X = 86;
    private static final int CHAT_BUTTON_Y = 52;
    private static final int ORE_ROW_X = 86;
    private static final int ORE_ROW_WIDTH = 164;
    private static final int ORE_ROW_HEIGHT = 13;
    private static final int ORE_LIST_START_Y = 67;
    private static final int ORE_LIST_MAX_ITEMS = 7;

    private final List<OreToggleManager.OreGroup> oreGroups = new ArrayList<>();
    private final OreRowWidget[] oreRows = new OreRowWidget[ORE_LIST_MAX_ITEMS];
    private int scrollOffset;
    private int maxScroll;

    public MiningTaskConfigGui(MiningTaskConfigContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);
        graphics.blit(BG, leftPos + PANEL_X_OFFSET, topPos + PANEL_Y_OFFSET, 0, 0, imageWidth, BG_HEIGHT);
    }

    @Override
    protected void initAdditionWidgets() {
        EntityMaid maid = getMaid();

        oreGroups.clear();
        oreGroups.addAll(OreToggleManager.getOreGroups());

        maxScroll = Math.max(0, oreGroups.size() - ORE_LIST_MAX_ITEMS);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        boolean enabled = MaidMineBreakTask.isChatNotifyEnabled(maid);
        Component label = Component.translatable("gui.mininglittlemaid.chat_notify");
        Component value = getToggleComponent(enabled);
        MaidConfigButton toggleBtn = new MaidConfigButton(
                leftPos + CHAT_BUTTON_X, topPos + CHAT_BUTTON_Y, label, value,
                btn -> {
                    MaidMineBreakTask.toggleChatNotify(maid);
                    boolean newState = MaidMineBreakTask.isChatNotifyEnabled(maid);
                    btn.setValue(getToggleComponent(newState));
                });
        addRenderableWidget(toggleBtn);

        int panelY = topPos + ORE_LIST_START_Y;
        for (int i = 0; i < ORE_LIST_MAX_ITEMS; i++) {
            int rowY = panelY + i * ORE_ROW_HEIGHT;
            OreRowWidget row = new OreRowWidget(
                    leftPos + ORE_ROW_X, rowY, ORE_ROW_WIDTH, ORE_ROW_HEIGHT);
            oreRows[i] = row;
            addRenderableWidget(row);
        }
        refreshOreRows();
    }

    private void refreshOreRows() {
        EntityMaid maid = getMaid();
        for (int i = 0; i < ORE_LIST_MAX_ITEMS; i++) {
            int dataIndex = scrollOffset + i;
            if (dataIndex < oreGroups.size()) {
                OreToggleManager.OreGroup group = oreGroups.get(dataIndex);
                boolean isEnabled = OreToggleManager.isOreGroupEnabled(maid, group.groupKey());
                oreRows[i].bind(group.representativeBlock().getName(), isEnabled, () -> {
                    OreToggleManager.toggleOreGroup(maid, group.groupKey());
                    refreshOreRows();
                });
            } else {
                oreRows[i].clear();
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (oreGroups.isEmpty()) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int panelLeft = leftPos + PANEL_X_OFFSET;
        int panelRight = panelLeft + imageWidth;
        int scrollTop = topPos + ORE_LIST_START_Y;
        int scrollBottom = scrollTop + ORE_LIST_MAX_ITEMS * ORE_ROW_HEIGHT;

        if (mouseX >= panelLeft && mouseX <= panelRight
                && mouseY >= scrollTop && mouseY <= scrollBottom) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - Math.signum(scrollY)));
            refreshOreRows();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderAddition(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (oreGroups.isEmpty()) {
            return;
        }
        Component title = Component.translatable("gui.mininglittlemaid.ore_toggles");
        graphics.drawString(font, title, leftPos + TITLE_X_OFFSET, topPos + TITLE_Y_OFFSET, 0x404040, false);

        if (maxScroll > 0) {
            int barWidth = 4;
            int barX = leftPos + PANEL_X_OFFSET + imageWidth - barWidth - 4;
            int scrollTopPixel = topPos + ORE_LIST_START_Y;
            int scrollHeight = ORE_LIST_MAX_ITEMS * ORE_ROW_HEIGHT;
            int totalRows = oreGroups.size();
            int visibleRows = ORE_LIST_MAX_ITEMS;
            int thumbHeight = Math.max(8, visibleRows * scrollHeight / totalRows);
            int trackHeight = scrollHeight - thumbHeight;
            int thumbY = trackHeight == 0 ? scrollTopPixel
                    : scrollTopPixel + scrollOffset * trackHeight / maxScroll;
            graphics.fill(barX, scrollTopPixel, barX + barWidth,
                    scrollTopPixel + scrollHeight, 0x33FFFFFF);
            graphics.fill(barX, thumbY, barX + barWidth, thumbY + thumbHeight, 0x99FFFFFF);
        }
    }

    private static MutableComponent getToggleComponent(boolean enabled) {
        return Component.translatable(enabled
                ? "gui.mininglittlemaid.option.on"
                : "gui.mininglittlemaid.option.off");
    }

    private static final class OreRowWidget extends AbstractWidget {
        private boolean hasData;
        private Component oreName = Component.empty();
        private boolean toggled;
        private Runnable onToggle;

        OreRowWidget(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
            this.hasData = false;
        }

        void bind(Component oreName, boolean toggled, Runnable onToggle) {
            this.oreName = oreName;
            this.toggled = toggled;
            this.onToggle = onToggle;
            this.hasData = true;
            this.visible = true;
            this.active = true;
        }

        void clear() {
            this.hasData = false;
            this.visible = false;
            this.active = false;
            this.onToggle = null;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (!hasData) {
                return;
            }
            graphics.enableScissor(getX(), getY(), getX() + width, getY() + height);
            if (isHovered) {
                graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x20FFFFFF);
            }
            graphics.drawString(Minecraft.getInstance().font, oreName,
                    getX() + 5, getY() + 3, 0xE0E0E0, false);
            Component value = Component.translatable(toggled
                    ? "gui.mininglittlemaid.option.on"
                    : "gui.mininglittlemaid.option.off");
            int valueColor = toggled ? 0x55FF55 : 0xFF5555;
            graphics.drawCenteredString(Minecraft.getInstance().font, value,
                    getX() + 142, getY() + 3, valueColor);
            graphics.disableScissor();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!hasData || !visible || !active) {
                return false;
            }
            if (mouseX >= getX() + 100 && mouseX <= getX() + width
                    && mouseY >= getY() && mouseY <= getY() + height) {
                if (onToggle != null) {
                    onToggle.run();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput output) {
        }
    }
}
