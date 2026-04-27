package com.github.tartaricacid.mining_little_maid.task;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 根据女仆好感度等级，决定当前可挖掘的矿石类型
 */
public class MiningFavorGate {

    /**
     * 检查在给定好感度等级下，是否可以挖掘该方块
     */
    public static boolean canMineAtLevel(BlockState state, int favorLevel) {
        if (state.is(BlockTags.COAL_ORES) || state.is(BlockTags.COPPER_ORES)) {
            return favorLevel >= 0;
        }
        if (state.is(BlockTags.IRON_ORES)) {
            return favorLevel >= 1;
        }
        if (state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.REDSTONE_ORES) || state.is(Blocks.NETHER_QUARTZ_ORE)) {
            return favorLevel >= 2;
        }
        if (state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES)
                || state.is(Blocks.ANCIENT_DEBRIS)) {
            return favorLevel >= 3;
        }
        return false;
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
