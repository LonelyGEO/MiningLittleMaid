package com.github.tartaricacid.mininglittlemaid.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class MaidMineBreakTask extends Behavior<EntityMaid> {
    private static final int CHECK_RATE = 20;
    private static final String CHAT_NOTIFY_KEY = "mining_chat_notify";
    private final TaskMining task;
    private long lastCheckTime;

    public MaidMineBreakTask(TaskMining task) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
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
                .filter(pos -> pos.distManhattan(maid.blockPosition()) <= 3)
                .filter(pos -> task.canHarvest(maid, pos, worldIn.getBlockState(pos)))
                .isPresent();
    }

    @Override
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        this.lastCheckTime = worldIn.getGameTime();
        maid.getBrain().getMemory(InitEntities.TARGET_POS.get()).ifPresent(posTracker -> {
            BlockPos targetPos = BlockPos.containing(posTracker.currentPosition());
            if (Math.abs(targetPos.getY() - maid.blockPosition().getY()) > 2) {
                maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
                worldIn.sendParticles(ParticleTypes.HAPPY_VILLAGER, maid.getX(), maid.getY() + 1.5, maid.getZ(),
                        3, 0.3, 0.3, 0.3, 0);
                maid.getChatBubbleManager().addTextChatBubble("message.mining_little_maid.ore_above_below");
                if (isChatNotifyEnabled(maid) && maid.getOwner() instanceof ServerPlayer player) {
                    player.sendSystemMessage(Component.translatable("message.mining_little_maid.ore_above_below"));
                }
                return;
            }
            task.harvest(maid, targetPos, worldIn.getBlockState(targetPos));
            BlockPos nextOre = findAdjacentOre(worldIn, maid);
            if (nextOre != null) {
                BehaviorUtils.setWalkAndLookTargetMemories(maid, nextOre, 0.6f, 2);
                maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(nextOre));
            } else {
                maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
            }
        });
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
        }
    }
}
