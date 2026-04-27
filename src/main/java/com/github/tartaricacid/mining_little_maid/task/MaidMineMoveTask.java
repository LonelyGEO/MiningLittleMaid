package com.github.tartaricacid.mining_little_maid.task;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;

public class MaidMineMoveTask extends MaidCheckRateTask {
    private static final int MAX_DELAY_TIME = 120;
    private static final float DEFAULT_SEARCH_RADIUS = 16.0F;
    private final TaskMining task;
    private final float movementSpeed;
    private final int verticalSearchRange;
    private BlockPos adjacentOrePos;

    public MaidMineMoveTask(TaskMining task, float movementSpeed, int verticalSearchRange) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT
        ));
        this.task = task;
        this.movementSpeed = movementSpeed;
        this.verticalSearchRange = verticalSearchRange;
        this.setMaxCheckRate(MAX_DELAY_TIME);
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTime) {
        if (maid.isHomeModeEnable()) {
            return;
        }
        float maxDistance = DEFAULT_SEARCH_RADIUS;
        MaidPathFindingBFS bfs = new MaidPathFindingBFS(
                maid.getNavigation().getNodeEvaluator(),
                world,
                maid,
                maxDistance,
                verticalSearchRange
        );
        this.adjacentOrePos = null;
        bfs.find(pos -> {
            for (Direction dir : Direction.values()) {
                BlockPos adjacent = pos.relative(dir);
                BlockState state = world.getBlockState(adjacent);
                if (MiningFavorGate.isMineableOre(state)
                        && task.canHarvest(maid, adjacent, state)) {
                    this.adjacentOrePos = adjacent.immutable();
                    return true;
                }
            }
            return false;
        });
        if (this.adjacentOrePos != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(maid, this.adjacentOrePos, movementSpeed, 0);
            maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(this.adjacentOrePos));
            this.setNextCheckTickCount(5);
        }
        bfs.finish();
    }
}
