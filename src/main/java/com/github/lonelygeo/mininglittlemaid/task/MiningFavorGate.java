package com.github.lonelygeo.mininglittlemaid.task;

import com.github.lonelygeo.mininglittlemaid.MiningLittleMaid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 好感度门控：好感度越高，女仆透过石头探测矿石的范围越远
 */
public class MiningFavorGate {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final TagKey<Block> MINEABLE_ORES =
            TagKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(MiningLittleMaid.MOD_ID, "mineable_ores"));

    public static final TagKey<Item> MINING_TOOLS =
            TagKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(MiningLittleMaid.MOD_ID, "mining_tools"));

    public static final TagKey<Item> WEAPONS =
            TagKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(MiningLittleMaid.MOD_ID, "weapons"));

    /**
     * 检查工具是否为可采矿工具（Item Tag 驱动）
     */
    public static boolean isMiningTool(ItemStack stack) {
        return stack.is(MINING_TOOLS);
    }

    /**
     * 检查物品是否为武器（Item Tag 驱动）
     */
    public static boolean isWeapon(ItemStack stack) {
        return stack.is(WEAPONS);
    }

    /**
     * 好感度等级 3 以上可连锁挖掘
     */
    public static boolean canVeinMine(int favorLevel) {
        return favorLevel >= 3;
    }

    /**
     * 同种矿石判定（iron_ore 与 deepslate_iron_ore 算同种）
     */
    public static boolean isSameOreType(BlockState a, BlockState b) {
        if (a.getBlock() == b.getBlock()) return true;
        return getBaseOreName(a).equals(getBaseOreName(b));
    }

    private static String getBaseOreName(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock())
                .getPath().replace("deepslate_", "");
    }

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

    public static boolean isMineableOre(BlockState state) {
        return state.is(MINEABLE_ORES);
    }
}
