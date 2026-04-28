package com.github.lonelygeo.mininglittlemaid.task;

import com.github.lonelygeo.mininglittlemaid.MiningLittleMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.github.lonelygeo.mininglittlemaid.config.Config;
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
        Config.debugLog(LOGGER,"Sniff radius: favorLevel={} -> radius={}", favorLevel, radius);
        return radius;
    }

    public static boolean isMineableOre(BlockState state) {
        return state.is(MINEABLE_ORES);
    }

    /**
     * 从 fromPos 出发，检查矿石 orePos 是否至少有一个暴露面能够无障碍到达。
     * 用于防止女仆透过墙体隔墙挖矿。
     * <p>
     * 区别于旧版仅检查 ore 的 6 方向是否有空气：旧检查只验证矿石「有暴露面」，
     * 不验证女仆能否实际走到那个暴露面。墙体后的矿石在另一侧走廊有暴露面即可通过旧检查，
     * 最终导致隔墙挖矿。本方法对每个暴露面做受限迷你 BFS，确认 fromPos 能否通过
     * 空气/可替换方块路径走到该暴露面前。
     *
     * @param world  当前世界
     * @param fromPos 起点的可达位置（BFS路径点 或 女仆当前位置）
     * @param orePos  矿石位置
     * @return true 如果至少有一个暴露面能够从 fromPos 通过空气路径到达
     */
    public static boolean hasReachableExposedFace(Level world, BlockPos fromPos, BlockPos orePos) {
        for (Direction dir : Direction.values()) {
            BlockPos accessPos = orePos.relative(dir);
            BlockState accessState = world.getBlockState(accessPos);
            if (accessState.isAir() || accessState.canBeReplaced()) {
                if (isAccessible(world, fromPos, accessPos, orePos)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 受限迷你 BFS：从 from 出发，能否仅通过空气/可替换方块走到 to。
     * 搜索范围按 from→to 曼哈顿距离 +1 限制，开销极低。
     */
    private static boolean isAccessible(Level world, BlockPos from, BlockPos to, BlockPos orePos) {
        if (from.equals(to)) return true;
        int maxDist = from.distManhattan(to);
        if (maxDist > 8) return false;

        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
        queue.add(from);
        visited.add(from);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current.distManhattan(from) > maxDist + 1) continue;

            for (Direction d : Direction.values()) {
                BlockPos next = current.relative(d);
                if (visited.contains(next)) continue;
                if (next.equals(to)) return true;
                if (next.equals(orePos)) continue;
                if (next.distManhattan(to) > maxDist) continue;

                BlockState state = world.getBlockState(next);
                if (state.isAir() || state.canBeReplaced()) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return false;
    }
}
