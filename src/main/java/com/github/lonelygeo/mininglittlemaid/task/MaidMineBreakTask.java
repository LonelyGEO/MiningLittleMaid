package com.github.lonelygeo.mininglittlemaid.task;

import com.github.lonelygeo.mininglittlemaid.config.Config;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class MaidMineBreakTask extends Behavior<EntityMaid> {
    private static final int CHECK_RATE = 20;
    private static final String CHAT_NOTIFY_KEY = "mining_chat_notify";
    private static final int CLOSE_ENOUGH_HORIZONTAL = 3;
    private static final Logger LOGGER = LogManager.getLogger();
    private final TaskMining task;
    private long lastCheckTime;

    public MaidMineBreakTask(TaskMining task) {
        super(ImmutableMap.of(
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT
        ));
        this.task = task;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid maid) {
        if (worldIn.getGameTime() - this.lastCheckTime < CHECK_RATE) {
            return false;
        }
        return maid.getBrain().getMemory(InitEntities.TARGET_POS.get())
                .map(PositionTracker::currentPosition)
                .map(BlockPos::containing)
                .filter(pos -> Math.abs(pos.getX() - maid.blockPosition().getX())
                        + Math.abs(pos.getZ() - maid.blockPosition().getZ()) <= CLOSE_ENOUGH_HORIZONTAL)
                .filter(pos -> task.canHarvest(maid, pos, worldIn.getBlockState(pos)))
                .isPresent();
    }

    @Override
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        this.lastCheckTime = worldIn.getGameTime();
        maid.getBrain().getMemory(InitEntities.TARGET_POS.get()).ifPresent(posTracker -> {
            BlockPos targetPos = BlockPos.containing(posTracker.currentPosition());
            int yDiff = targetPos.getY() - maid.blockPosition().getY();
            if (yDiff > 3 || yDiff < -2) {
                if (!task.canAlertOre(targetPos, worldIn.getGameTime())) {
                    maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
                    maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                    return;
                }
                Config.debugLog(LOGGER,"Ore at {} unreachable (yDiff={}), sending above/below message", targetPos, yDiff);
                maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
                maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                worldIn.sendParticles(ParticleTypes.HAPPY_VILLAGER, maid.getX(), maid.getY() + 1.5, maid.getZ(),
                        3, 0.3, 0.3, 0.3, 0);
                BlockState targetState = worldIn.getBlockState(targetPos);
                Component oreName = targetState.getBlock().getName();
                String kaomoji = randomKaomoji();
                String directionHint = yDiff > 3 ? "↑" : "↓";
                String bubbleText = directionHint + " " + oreName.getString() + " " + kaomoji;
                maid.getChatBubbleManager().addTextChatBubble(bubbleText);
                String directionKey = yDiff > 3
                        ? "message.mininglittlemaid.ore_above"
                        : "message.mininglittlemaid.ore_below";
                Component msg = Component.translatable(directionKey,
                        maid.getDisplayName(), oreName)
                        .append(Component.literal(" " + kaomoji));
                if (isChatNotifyEnabled(maid) && maid.getOwner() instanceof ServerPlayer player) {
                    player.sendSystemMessage(msg);
                }
                task.setOrePauseEndTime(worldIn.getGameTime() + 100);
                return;
            }
            Config.debugLog(LOGGER,"Mining ore at {}", targetPos);
            if (!MiningFavorGate.hasReachableExposedFace(worldIn, maid.blockPosition(), targetPos)) {
                Config.debugLog(LOGGER,"Ore at {} not reachable from maid position, clearing target", targetPos);
                maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
                return;
            }
            BlockState targetState = worldIn.getBlockState(targetPos);
            int count;
            if (MiningFavorGate.canVeinMine(maid.getFavorabilityManager().getLevel())) {
                count = veinMineBFS(worldIn, maid, targetPos, targetState);
            } else {
                task.harvest(maid, targetPos, targetState);
                count = 1;
            }
            ItemStack tool = maid.getMainHandItem();
            if (count > 0 && MiningFavorGate.isMiningTool(tool)) {
                tool.hurtAndBreak(count, maid, EquipmentSlot.MAINHAND);
            }
            if (count > 1) {
                Config.debugLog(LOGGER,"Vein mined {} ores", count);
                worldIn.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        maid.getX(), maid.getY() + 1.5, maid.getZ(),
                        count * 2, 0.5, 0.5, 0.5, 0.1);
            }
            BlockPos nextOre = findAdjacentOre(worldIn, maid);
            if (nextOre != null) {
                Config.debugLog(LOGGER,"Adjacent ore found at {}, continuing chain", nextOre);
                BehaviorUtils.setWalkAndLookTargetMemories(maid, nextOre, 0.6f, 2);
                maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(nextOre));
            } else {
                Config.debugLog(LOGGER,"No adjacent ore, clearing target");
                maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
            }
        });
    }

    private int veinMineBFS(ServerLevel world, EntityMaid maid, BlockPos startPos, BlockState firstState) {
        int maxVeinSize = Config.MAX_VEIN_SIZE.get();
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(startPos);
        visited.add(startPos);
        int count = 0;
        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();

        while (!queue.isEmpty() && count < maxVeinSize) {
            BlockPos current = queue.poll();

            for (Direction dir : Direction.values()) {
                neighborPos.setWithOffset(current, dir);
                if (!visited.contains(neighborPos)) {
                    visited.add(neighborPos.immutable());
                    BlockState neighborState = world.getBlockState(neighborPos);
                    if (MiningFavorGate.isMineableOre(neighborState)
                            && MiningFavorGate.isSameOreType(firstState, neighborState)) {
                        queue.add(neighborPos.immutable());
                    }
                }
            }

            BlockState currentState = world.getBlockState(current);
            if (!MiningFavorGate.isMineableOre(currentState) || !maid.canDestroyBlock(current)) {
                continue;
            }
            if (!maid.destroyBlock(current)) {
                continue;
            }
            count++;
        }
        return count;
    }

    private BlockPos findAdjacentOre(ServerLevel world, EntityMaid maid) {
        BlockPos maidPos = maid.blockPosition();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    mutablePos.setWithOffset(maidPos, x, y, z);
                    BlockState state = world.getBlockState(mutablePos);
                    if (MiningFavorGate.isMineableOre(state)
                            && task.canHarvest(maid, mutablePos, state)) {
                        return mutablePos.immutable();
                    }
                }
            }
        }
        return null;
    }

    public static boolean isChatNotifyEnabled(EntityMaid maid) {
        return !maid.getPersistentData().contains(CHAT_NOTIFY_KEY)
                || maid.getPersistentData().getBoolean(CHAT_NOTIFY_KEY);
    }

    public static void toggleChatNotify(EntityMaid maid) {
        maid.getPersistentData().putBoolean(CHAT_NOTIFY_KEY, !isChatNotifyEnabled(maid));
    }

    public static void toggleForMaid(net.minecraft.world.level.Level level, int maidId) {
        if (level.getEntity(maidId) instanceof EntityMaid maid) {
            toggleChatNotify(maid);
            Config.debugLog(LOGGER,"Chat notify toggled, maidId={}, enabled={}", maidId, isChatNotifyEnabled(maid));
        }
    }

    private static final String[] KAOMOJI = {
        "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧",
        "☆*:.｡.o(≧▽≦)o.｡.:*☆",
        "ヽ(>∀<☆)ノ",
        "(๑˃̵ᴗ˂̵)و",
        "✧⁺⸜(●′▾‵●)⸝⁺✧"
    };

    private static String randomKaomoji() {
        return KAOMOJI[new Random().nextInt(KAOMOJI.length)];
    }
}
