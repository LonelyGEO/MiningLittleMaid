package com.github.tartaricacid.mining_little_maid.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class MaidMineBreakTask extends Behavior<EntityMaid> {
    private static final int CHECK_RATE = 20;
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
            task.harvest(maid, targetPos, worldIn.getBlockState(targetPos));
            maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
        });
    }
}
