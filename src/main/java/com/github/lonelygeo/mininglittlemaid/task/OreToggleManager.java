package com.github.lonelygeo.mininglittlemaid.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public final class OreToggleManager {
    private static final String ORE_DISABLED_KEY = "mining_ore_disabled";

    private OreToggleManager() {
    }

    public static boolean isOreEnabled(EntityMaid maid, BlockState state) {
        String groupKey = MiningFavorGate.getOreGroupKey(state);
        return !getDisabledGroups(maid).contains(groupKey);
    }

    public static boolean isOreGroupEnabled(EntityMaid maid, String groupKey) {
        return !getDisabledGroups(maid).contains(groupKey);
    }

    public static void toggleOreGroup(EntityMaid maid, String groupKey) {
        Set<String> disabled = getDisabledGroups(maid);
        if (disabled.contains(groupKey)) {
            disabled.remove(groupKey);
        } else {
            disabled.add(groupKey);
        }
        setDisabledGroups(maid, disabled);
    }

    public static Set<String> getDisabledGroups(EntityMaid maid) {
        String raw = maid.getPersistentData().getString(ORE_DISABLED_KEY);
        if (raw.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(raw.split(",")));
    }

    private static void setDisabledGroups(EntityMaid maid, Set<String> groups) {
        if (groups.isEmpty()) {
            maid.getPersistentData().remove(ORE_DISABLED_KEY);
        } else {
            maid.getPersistentData().putString(ORE_DISABLED_KEY, String.join(",", groups));
        }
    }

    public static List<OreGroup> getOreGroups() {
        Map<String, Block> groupMap = new LinkedHashMap<>();

        for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(MiningFavorGate.MINEABLE_ORES)) {
            Block block = holder.value();
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            String groupKey = MiningFavorGate.getOreGroupKey(id.getPath());
            groupMap.putIfAbsent(groupKey, block);
        }

        List<OreGroup> groups = new ArrayList<>();
        for (Map.Entry<String, Block> entry : groupMap.entrySet()) {
            groups.add(new OreGroup(entry.getKey(), entry.getValue()));
        }
        return groups;
    }

    public static final class OreGroup {
        private final String groupKey;
        private final Block representativeBlock;

        OreGroup(String groupKey, Block representativeBlock) {
            this.groupKey = groupKey;
            this.representativeBlock = representativeBlock;
        }

        public String groupKey() {
            return groupKey;
        }

        public Block representativeBlock() {
            return representativeBlock;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OreGroup that)) return false;
            return groupKey.equals(that.groupKey);
        }

        @Override
        public int hashCode() {
            return groupKey.hashCode();
        }
    }
}
