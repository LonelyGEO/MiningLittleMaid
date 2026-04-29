package com.github.lonelygeo.mininglittlemaid.task;

import com.github.lonelygeo.mininglittlemaid.api.event.MiningMessageEvent;
import com.github.lonelygeo.mininglittlemaid.api.event.MiningMessageType;
import com.github.lonelygeo.mininglittlemaid.config.Config;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.util.TaskEquipUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaidMineCombatCheckTask extends MaidCheckRateTask {
    private static final String ATTACK_TASK_ID = "touhou_little_maid:attack";
    private static final String RESUME_KEY = "mining_resume";
    private static final String COOLDOWN_KEY = "mining_combat_cooldown";
    private static final Logger LOGGER = LogManager.getLogger();

    public MaidMineCombatCheckTask() {
        super(ImmutableMap.of());
        this.setMaxCheckRate(Config.COMBAT_CHECK_RATE.get());
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTime) {
        if (maid.getFavorabilityManager().getLevel() < 1) {
            Config.debugLog(LOGGER,"Combat: skip, favor level < 1");
            return;
        }

        long cooldown = maid.getPersistentData().getLong(COOLDOWN_KEY);
        if (gameTime - cooldown < Config.COMBAT_COOLDOWN_TICKS.get()) {
            Config.debugLog(LOGGER,"Combat: skip, cooldown active ({}t remaining)",
                    Config.COMBAT_COOLDOWN_TICKS.get() - (gameTime - cooldown));
            return;
        }

        AABB searchArea = maid.getBoundingBox().inflate(
                Config.COMBAT_SEARCH_HORIZONTAL.get(),
                Config.COMBAT_SEARCH_VERTICAL.get(),
                Config.COMBAT_SEARCH_HORIZONTAL.get());
        List<Monster> monsters = world.getEntitiesOfClass(Monster.class, searchArea,
                m -> m.isAlive() && maid.canAttack(m));
        if (monsters.isEmpty()) {
            Config.debugLog(LOGGER,"Combat: no monsters in range H={}, V={}",
                    Config.COMBAT_SEARCH_HORIZONTAL.get(), Config.COMBAT_SEARCH_VERTICAL.get());
            return;
        }
        Config.debugLog(LOGGER,"Combat: {} monster(s) detected", monsters.size());

        IMaidTask currentTask = maid.getTask();
        if (currentTask != null && ATTACK_TASK_ID.equals(currentTask.getUid().toString())) {
            Config.debugLog(LOGGER,"Combat: already in attack task, skip");
            return;
        }

        if (!TaskEquipUtil.tryEquipFromBackpack(maid, MiningFavorGate::isWeapon)) {
            Config.debugLog(LOGGER,"Combat: no weapon found, skip");
            return;
        }
        Config.debugLog(LOGGER,"Combat detected, equipping weapon and switching to attack task");

        Component bubbleText = Component.literal("Monsters! (｀・ω・´)");
        maid.getChatBubbleManager().addTextChatBubble(bubbleText.getString());
        Component msg = Component.translatable("message.mininglittlemaid.combat_detected",
                maid.getDisplayName(), monsters.size());
        Map<String, Object> context = new HashMap<>();
        context.put("monster_count", monsters.size());
        MiningMessageEvent event = new MiningMessageEvent(maid,
                MiningMessageType.COMBAT_DETECTED, null,
                bubbleText, msg, context);
        NeoForge.EVENT_BUS.post(event);
        if (!event.isCanceled() && MaidMineBreakTask.isChatNotifyEnabled(maid)
                && maid.getOwner() instanceof ServerPlayer player) {
            player.sendSystemMessage(msg);
        }

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
