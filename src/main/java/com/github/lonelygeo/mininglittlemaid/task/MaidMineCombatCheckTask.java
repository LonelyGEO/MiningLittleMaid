package com.github.lonelygeo.mininglittlemaid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.util.TaskEquipUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import com.github.lonelygeo.mininglittlemaid.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class MaidMineCombatCheckTask extends MaidCheckRateTask {
    private static final int CHECK_RATE = 60;
    private static final String ATTACK_TASK_ID = "touhou_little_maid:attack";
    private static final String RESUME_KEY = "mining_resume";
    private static final String COOLDOWN_KEY = "mining_combat_cooldown";
    private static final long COOLDOWN_TICKS = 200;
    private static final Logger LOGGER = LogManager.getLogger();

    public MaidMineCombatCheckTask() {
        super(ImmutableMap.of());
        this.setMaxCheckRate(CHECK_RATE);
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTime) {
        if (maid.getFavorabilityManager().getLevel() < 1) {
            return;
        }

        long cooldown = maid.getPersistentData().getLong(COOLDOWN_KEY);
        if (gameTime - cooldown < COOLDOWN_TICKS) {
            return;
        }

        AABB searchArea = maid.getBoundingBox().inflate(10.0, 5.0, 10.0);
        List<Monster> monsters = world.getEntitiesOfClass(Monster.class, searchArea,
                m -> m.isAlive() && maid.canAttack(m));
        if (monsters.isEmpty()) {
            return;
        }

        IMaidTask currentTask = maid.getTask();
        if (currentTask != null && ATTACK_TASK_ID.equals(currentTask.getUid().toString())) {
            return;
        }

        if (!TaskEquipUtil.tryEquipFromBackpack(maid, MiningFavorGate::isWeapon)) {
            return;
        }
        Config.debugLog(LOGGER,"Combat detected, equipping weapon and switching to attack task");

        if (currentTask != null) {
            maid.getPersistentData().putString(RESUME_KEY, currentTask.getUid().toString());
        }

        TaskManager.findTask(ResourceLocation.parse(ATTACK_TASK_ID))
                .ifPresent(task -> {
                    maid.setTask(task);
                    Config.debugLog(LOGGER,"Switched to attack task, will resume mining after combat");
                });
    }
}
