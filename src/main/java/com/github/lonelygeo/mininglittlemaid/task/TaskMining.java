package com.github.lonelygeo.mininglittlemaid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.FunctionCallSwitchResult;
import com.github.tartaricacid.touhoulittlemaid.api.task.IFarmTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.lonelygeo.mininglittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.github.tartaricacid.touhoulittlemaid.util.TaskEquipUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import com.github.lonelygeo.mininglittlemaid.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class TaskMining implements IFarmTask {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("mining_little_maid", "mining");
    private static final int VERTICAL_SEARCH_RANGE = 16;
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return Items.IRON_PICKAXE.getDefaultInstance();
    }

    @Override
    public boolean isEnable(EntityMaid maid) {
        return !maid.isHomeModeEnable();
    }

    @Override
    public boolean isSeed(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canHarvest(EntityMaid maid, BlockPos cropPos, BlockState cropState) {
        if (!hasPickaxe(maid)) {
            return false;
        }
        return MiningFavorGate.isMineableOre(cropState);
    }

    @Override
    public void harvest(EntityMaid maid, BlockPos cropPos, BlockState cropState) {
        Config.debugLog(LOGGER,"Destroying block at {}", cropPos);
        if (maid.canDestroyBlock(cropPos)) {
            maid.destroyBlock(cropPos);
        }
    }

    @Override
    public boolean canPlant(EntityMaid maid, BlockPos basePos, BlockState baseState, ItemStack seed) {
        return false;
    }

    @Override
    public ItemStack plant(EntityMaid maid, BlockPos basePos, BlockState baseState, ItemStack seed) {
        return seed;
    }

    @Override
    public boolean checkCropPosAbove() {
        return false;
    }

    @Override
    public double getCloseEnoughDist() {
        return 2.0;
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid maid) {
        return SoundUtil.environmentSound(maid, InitSounds.MAID_MINING.get(), 0.5f);
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        MaidMineDurabilityCheckTask durabilityTask = new MaidMineDurabilityCheckTask();
        MaidMineInventoryCheckTask inventoryTask = new MaidMineInventoryCheckTask();
        MaidMineTorchPlaceTask torchTask = new MaidMineTorchPlaceTask();
        MaidMineCombatCheckTask combatTask = new MaidMineCombatCheckTask();
        MaidMineMoveTask moveTask = new MaidMineMoveTask(this, 0.6f, VERTICAL_SEARCH_RANGE);
        MaidMineBreakTask breakTask = new MaidMineBreakTask(this);
        return Lists.newArrayList(
                Pair.of(4, durabilityTask),
                Pair.of(4, inventoryTask),
                Pair.of(4, torchTask),
                Pair.of(4, combatTask),
                Pair.of(5, moveTask),
                Pair.of(6, breakTask)
        );
    }

    @Override
    public String getMaidActionSummary() {
        return "Mine ores with detection range based on favor level";
    }

    @Override
    public FunctionCallSwitchResult onFunctionCallSwitch(EntityMaid maid) {
        if (MiningFavorGate.isMiningTool(maid.getMainHandItem())) {
            return FunctionCallSwitchResult.OK;
        }
        if (TaskEquipUtil.tryEquipFromBackpack(maid, MiningFavorGate::isMiningTool)) {
            Config.debugLog(LOGGER,"Task switch: equipping mining tool from backpack");
            return FunctionCallSwitchResult.OK;
        }
        return FunctionCallSwitchResult.MISSING_REQUIRED_ITEM;
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Collections.singletonList(Pair.of("has_pickaxe", this::hasPickaxe));
    }

    private boolean hasPickaxe(EntityMaid maid) {
        if (MiningFavorGate.isMiningTool(maid.getMainHandItem())) {
            return true;
        }
        return ItemsUtil.isStackIn(maid.getAvailableInv(false), MiningFavorGate::isMiningTool);
    }
}
