package com.github.lonelygeo.mininglittlemaid.task;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 好感度门控：好感度越高，女仆透过石头探测矿石的范围越远
 */
public class MiningFavorGate {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 根据好感度等级获取嗅探半径（透过石头的检测距离）
     */
    public static int getSniffRadius(int favorLevel) {
        int radius;
        if (favorLevel >= 3) radius = 3;
        else if (favorLevel >= 2) radius = 2;
        else if (favorLevel >= 1) radius = 2;
        else radius = 1;
        LOGGER.debug("Sniff radius: favorLevel={} -> radius={}", favorLevel, radius);
        return radius;
    }

    /**
     * 检查在给定好感度等级下，是否可以挖掘该方块
     */
    public static boolean canMineAtLevel(BlockState state, int favorLevel) {
        return isMineableOre(state);
    }

    /**
     * 检查该方块是否为可挖掘的矿石（不考虑等级）
     */
    public static boolean isMineableOre(BlockState state) {
        return state.is(BlockTags.COAL_ORES) || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES)
                || state.is(Blocks.NETHER_QUARTZ_ORE) || state.is(Blocks.ANCIENT_DEBRIS);
    }
}
