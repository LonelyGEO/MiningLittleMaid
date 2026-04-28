package com.github.lonelygeo.mininglittlemaid.task;

import com.github.lonelygeo.mininglittlemaid.api.event.MiningMessageEvent;
import com.github.lonelygeo.mininglittlemaid.api.event.MiningMessageType;
import com.github.lonelygeo.mininglittlemaid.config.Config;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class MaidMineInventoryCheckTask extends MaidCheckRateTask {
    private static final String FULL_NOTIFY_KEY = "message.mininglittlemaid.inventory_full";
    private static final Logger LOGGER = LogManager.getLogger();

    public MaidMineInventoryCheckTask() {
        super(ImmutableMap.of());
        this.setMaxCheckRate(Config.INVENTORY_CHECK_RATE.get());
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTime) {
        if (isInventoryFull(maid)) {
            Config.debugLog(LOGGER,"Maid inventory full, cancelling mining task");
            Component msg = Component.translatable(FULL_NOTIFY_KEY, maid.getDisplayName());
            MiningMessageEvent event = new MiningMessageEvent(maid,
                    MiningMessageType.INVENTORY_FULL, null,
                    Component.empty(), msg, new HashMap<>());
            NeoForge.EVENT_BUS.post(event);
            if (!event.isCanceled() && maid.getOwner() instanceof ServerPlayer player) {
                player.sendSystemMessage(msg);
            }
            maid.setTask(null);
        }
    }

    static boolean isInventoryFull(EntityMaid maid) {
        CombinedInvWrapper inv = (CombinedInvWrapper) maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) return false;
            if (stack.getCount() < stack.getMaxStackSize()) return false;
        }
        return true;
    }
}
