package com.github.lonelygeo.mininglittlemaid.task;

import com.github.lonelygeo.mininglittlemaid.config.Config;
import com.github.lonelygeo.mininglittlemaid.api.event.MiningMessageEvent;
import com.github.lonelygeo.mininglittlemaid.api.event.MiningMessageType;
import net.neoforged.neoforge.common.NeoForge;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class MaidMineTorchPlaceTask extends MaidCheckRateTask {
    private static final int CHECK_RATE = 60;
    private static final String NO_TORCH_KEY = "message.mininglittlemaid.no_torch";

    private static final Logger LOGGER = LogManager.getLogger();
    private BlockPos lastPos = BlockPos.ZERO;
    private long lastPlaceTime;
    private long lastTorchNotifyTime;

    public MaidMineTorchPlaceTask() {
        super(ImmutableMap.of());
        this.setMaxCheckRate(CHECK_RATE);
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTime) {
        if (maid.blockPosition().equals(lastPos)) {
            return;
        }
        lastPos = maid.blockPosition().immutable();

        if (world.getMaxLocalRawBrightness(maid.blockPosition()) >= Config.MIN_LIGHT_LEVEL.get()) {
            return;
        }

        if (world.canSeeSky(maid.blockPosition())) {
            return;
        }

        long cooldown = Config.TORCH_COOLDOWN_TICKS.get();
        if (gameTime - lastPlaceTime < cooldown) {
            return;
        }

        BlockPos placePos = findPlaceableSurface(world, maid.blockPosition());
        if (placePos == null) {
            return;
        }

        if (!consumeTorch(maid)) {
            if (gameTime - lastTorchNotifyTime >= Config.TORCH_NOTIFY_COOLDOWN_TICKS.get()) {
                Component msg = Component.translatable(NO_TORCH_KEY, maid.getDisplayName());
                MiningMessageEvent event = new MiningMessageEvent(maid,
                        MiningMessageType.NO_TORCH, null,
                        Component.empty(), msg, new HashMap<>());
                NeoForge.EVENT_BUS.post(event);
                if (!event.isCanceled() && maid.getOwner() instanceof ServerPlayer player) {
                    player.sendSystemMessage(msg);
                }
                lastTorchNotifyTime = gameTime;
            }
            return;
        }

        world.setBlock(placePos, Blocks.TORCH.defaultBlockState(), 3);
        lastPlaceTime = gameTime;
        Config.debugLog(LOGGER,"Placed torch at {}", placePos);
    }

    private BlockPos findPlaceableSurface(ServerLevel world, BlockPos maidPos) {
        BlockPos.MutableBlockPos check = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy >= -3; dy--) {
            check.set(maidPos.getX(), maidPos.getY() + dy, maidPos.getZ());
            if (world.getBlockState(check).isFaceSturdy(world, check, Direction.UP)
                    && world.getBlockState(check.above()).isAir()) {
                return check.above().immutable();
            }
        }
        return null;
    }

    private boolean consumeTorch(EntityMaid maid) {
        CombinedInvWrapper inv = (CombinedInvWrapper) maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).is(Items.TORCH)) {
                inv.extractItem(i, 1, false);
                return true;
            }
        }
        return false;
    }
}
