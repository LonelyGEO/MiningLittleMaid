package com.github.lonelygeo.mininglittlemaid.task;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import com.github.lonelygeo.mininglittlemaid.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MaidMineInventoryCheckTask extends MaidCheckRateTask {
    private static final int CHECK_RATE = 60;
    private static final String FULL_NOTIFY_KEY = "message.mining_little_maid.inventory_full";
    private static final Logger LOGGER = LogManager.getLogger();

    public MaidMineInventoryCheckTask() {
        super(ImmutableMap.of());
        this.setMaxCheckRate(CHECK_RATE);
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTime) {
        if (isInventoryFull(maid)) {
            Config.debugLog(LOGGER,"Maid inventory full, cancelling mining task");
            if (maid.getOwner() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable(FULL_NOTIFY_KEY, maid.getDisplayName()));
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
