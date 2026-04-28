package com.github.lonelygeo.mininglittlemaid.event;

import com.github.lonelygeo.mininglittlemaid.MiningLittleMaid;
import com.github.lonelygeo.mininglittlemaid.config.Config;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = MiningLittleMaid.MOD_ID)
public class MaidMineCombatEventHandler {
    private static final String RESUME_KEY = "mining_resume";
    private static final String COOLDOWN_KEY = "mining_combat_cooldown";
    private static final Map<UUID, Integer> IDLE_COUNTER = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onMaidTick(MaidTickEvent event) {
        EntityMaid maid = event.getMaid();
        if (maid.level().isClientSide()) {
            return;
        }
        UUID id = maid.getUUID();
        String taskId = maid.getPersistentData().getString(RESUME_KEY);
        if (taskId.isEmpty()) {
            IDLE_COUNTER.remove(id);
            return;
        }

        if (maid.getTarget() != null) {
            IDLE_COUNTER.remove(id);
            Config.debugLog(LOGGER,"CombatEvent: maid has target, resetting idle counter");
            return;
        }

        int ticks = IDLE_COUNTER.getOrDefault(id, 0) + 1;
        Config.debugLog(LOGGER,"CombatEvent: idle ticks={}", ticks);
        if (ticks >= Config.COMBAT_RETURN_DELAY_TICKS.get()) {
            TaskManager.findTask(ResourceLocation.parse(taskId)).ifPresent(task -> {
                maid.setTask(task);
                maid.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                maid.getPersistentData().putLong(COOLDOWN_KEY, maid.level().getGameTime());
                Config.debugLog(LOGGER,"Combat ended, resuming task: {}", taskId);
            });
            maid.getPersistentData().remove(RESUME_KEY);
            IDLE_COUNTER.remove(id);
        } else {
            IDLE_COUNTER.put(id, ticks);
        }
    }
}
