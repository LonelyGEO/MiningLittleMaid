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
    private static final Logger LOGGER = LogManager.getLogger();
    private final TaskMining task;
    private final float movementSpeed;
    private BlockPos adjacentOrePos;

    public MaidMineMoveTask(TaskMining task, float movementSpeed) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT
        ));
        this.task = task;
        this.movementSpeed = movementSpeed;
        this.setMaxCheckRate(Config.BFS_MAX_DELAY.get());
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTime) {
        if (maid.isHomeModeEnable()) {
            Config.debugLog(LOGGER,"Move: skip, home mode enabled");
            return;
        }
        if (task.isOrePaused(gameTime)) {
            Config.debugLog(LOGGER,"Move: skip, ore search paused");
            return;
        }
        float maxDistance = Config.BFS_SEARCH_RADIUS.get();
        int verticalSearchRange = Config.BFS_VERTICAL_RANGE.get();
        MaidPathFindingBFS bfs = new MaidPathFindingBFS(
                maid.getNavigation().getNodeEvaluator(),
                world,
                maid,
                maxDistance,
                verticalSearchRange
        );
        this.adjacentOrePos = null;
        int sniffRadiusH = MiningFavorGate.getSniffRadiusHorizontal(maid.getFavorabilityManager().getLevel());
        int sniffRadiusV = MiningFavorGate.getSniffRadiusVertical(maid.getFavorabilityManager().getLevel());
        Config.debugLog(LOGGER,"BFS search started, sniffH={}, sniffV={}, maxDistance={}, verticalRange={}", sniffRadiusH, sniffRadiusV, maxDistance, verticalSearchRange);
        bfs.find(pos -> {
            for (int dx = -sniffRadiusH; dx <= sniffRadiusH; dx++) {
                for (int dy = -sniffRadiusV; dy <= sniffRadiusV; dy++) {
                    for (int dz = -sniffRadiusH; dz <= sniffRadiusH; dz++) {
                        BlockPos checkPos = pos.offset(dx, dy, dz);
                        BlockState state = world.getBlockState(checkPos);
                        if (MiningFavorGate.isMineableOre(maid, state)
                                && task.canHarvest(maid, checkPos, state)
                                && MiningFavorGate.hasReachableExposedFace(world, pos, checkPos)) {
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
            BlockPos ceilingOre = scanCeilingOres(world, maid);
            if (ceilingOre != null) {
                Config.debugLog(LOGGER,"Ceiling ore found at {} via vertical scan, setting walk and look target", ceilingOre);
                BehaviorUtils.setWalkAndLookTargetMemories(maid, ceilingOre, movementSpeed, 2);
                maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(ceilingOre));
            } else {
                Config.debugLog(LOGGER,"No ore from BFS or ceiling scan, wandering near owner");
                BlockPos ownerPos = maid.getOwner().blockPosition();
            int wanderH = Config.WANDER_RADIUS_H.get();
            int wanderV = Config.WANDER_RADIUS_V.get();
            int x = ownerPos.getX() + maid.getRandom().nextInt(wanderH) - wanderH / 2;
            int y = ownerPos.getY() + maid.getRandom().nextInt(wanderV) - wanderV / 2;
            int z = ownerPos.getZ() + maid.getRandom().nextInt(wanderH) - wanderH / 2;
                BehaviorUtils.setWalkAndLookTargetMemories(maid, new BlockPos(x, y, z), movementSpeed * 0.5f, 2);
            }
        }
        bfs.finish();
    }

    private BlockPos scanCeilingOres(ServerLevel world, EntityMaid maid) {
        BlockPos maidPos = maid.blockPosition();
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        int startY = maidPos.getY() + Config.CEILING_SCAN_Y_START.get();
        int endY = maidPos.getY() + Config.CEILING_SCAN_Y_END.get();
        int hRange = Config.CEILING_SCAN_HORIZONTAL.get();
        for (int y = startY; y <= endY; y++) {
            for (int dx = -hRange; dx <= hRange; dx++) {
                for (int dz = -hRange; dz <= hRange; dz++) {
                    checkPos.set(maidPos.getX() + dx, y, maidPos.getZ() + dz);
                    BlockState state = world.getBlockState(checkPos);
                    if (MiningFavorGate.isMineableOre(maid, state)
                            && task.canHarvest(maid, checkPos, state)
                            && MiningFavorGate.hasExposedFace(world, checkPos)) {
                        return checkPos.immutable();
                    }
                }
            }
        }
        return null;
    }
}
