package com.github.tartaricacid.mining_little_maid.task;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class MaidMineMoveTask extends MaidMoveToBlockTask {
    private final TaskMining task;

    public MaidMineMoveTask(TaskMining task, float movementSpeed, int verticalSearchStart, int verticalSearchRange) {
        super(movementSpeed, verticalSearchRange);
        this.verticalSearchStart = verticalSearchStart;
        this.task = task;
    }

    @Override
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        this.searchForDestination(worldIn, maid);
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel worldIn, EntityMaid maid, BlockPos basePos) {
        BlockState state = worldIn.getBlockState(basePos);
        if (!MiningFavorGate.isMineableOre(state)) {
            return false;
        }
        return task.canHarvest(maid, basePos, state);
    }
}
