package com.github.lonelygeo.mininglittlemaid.task;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import com.github.lonelygeo.mininglittlemaid.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MaidMineMoveTask extends MaidCheckRateTask {
    private static final int MAX_DELAY_TIME = 120;
    private static final float DEFAULT_SEARCH_RADIUS = 16.0F;
    private static final Logger LOGGER = LogManager.getLogger();
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
        int sniffRadius = MiningFavorGate.getSniffRadius(maid.getFavorabilityManager().getLevel());
        Config.debugLog(LOGGER,"BFS search started, sniffRadius={}, maxDistance={}, verticalRange={}", sniffRadius, maxDistance, verticalSearchRange);
        bfs.find(pos -> {
            for (int dx = -sniffRadius; dx <= sniffRadius; dx++) {
                for (int dy = -sniffRadius; dy <= sniffRadius; dy++) {
                    for (int dz = -sniffRadius; dz <= sniffRadius; dz++) {
                        BlockPos checkPos = pos.offset(dx, dy, dz);
                        BlockState state = world.getBlockState(checkPos);
                        if (MiningFavorGate.isMineableOre(state)
                                && task.canHarvest(maid, checkPos, state)) {
                            this.adjacentOrePos = checkPos.immutable();
                            return true;
                        }
                    }
                }
            }
            return false;
        });
        if (this.adjacentOrePos != null) {
            Config.debugLog(LOGGER,"Ore found at {} via BFS, setting walk and look target", this.adjacentOrePos);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, this.adjacentOrePos, movementSpeed, 2);
            maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(this.adjacentOrePos));
            this.setNextCheckTickCount(5);
        } else if (maid.getOwner() != null) {
            Config.debugLog(LOGGER,"No ore in BFS range, wandering near owner");
            BlockPos ownerPos = maid.getOwner().blockPosition();
            int x = ownerPos.getX() + maid.getRandom().nextInt(12) - 6;
            int y = ownerPos.getY() + maid.getRandom().nextInt(4) - 2;
            int z = ownerPos.getZ() + maid.getRandom().nextInt(12) - 6;
            BehaviorUtils.setWalkAndLookTargetMemories(maid, new BlockPos(x, y, z), movementSpeed * 0.5f, 2);
        }
        bfs.finish();
    }
}
