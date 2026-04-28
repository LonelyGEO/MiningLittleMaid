package com.github.lonelygeo.mininglittlemaid.task;

import com.github.lonelygeo.mininglittlemaid.config.Config;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.TaskEquipUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MaidMineDurabilityCheckTask extends MaidCheckRateTask {
    private static final Logger LOGGER = LogManager.getLogger();

    public MaidMineDurabilityCheckTask() {
        super(ImmutableMap.of());
        this.setMaxCheckRate(Config.DURABILITY_CHECK_RATE.get());
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTime) {
        int favorLevel = maid.getFavorabilityManager().getLevel();
        if (favorLevel < 1) {
            Config.debugLog(LOGGER,"Durability: skip, favor level < 1");
            return;
        }
        if (hasDurableTool(maid)) {
            Config.debugLog(LOGGER,"Durability: tool durability OK");
            return;
        }
        int minDurability = Config.MAX_VEIN_SIZE.get();
        if (TaskEquipUtil.tryEquipFromBackpack(maid, stack ->
                MiningFavorGate.isMiningTool(stack)
                        && (stack.getMaxDamage() - stack.getDamageValue()) >= minDurability)) {
            Config.debugLog(LOGGER,"Swapped to spare mining tool, remaining durability >= {}", minDurability);
            return;
        }
        Config.debugLog(LOGGER,"No durable mining tool available, cancelling mining task");
        maid.setTask(null);
    }

    private boolean hasDurableTool(EntityMaid maid) {
        ItemStack mainHand = maid.getMainHandItem();
        if (MiningFavorGate.isMiningTool(mainHand)) {
            return (mainHand.getMaxDamage() - mainHand.getDamageValue()) >= Config.MAX_VEIN_SIZE.get();
        }
        return false;
    }
}
