package com.github.lonelygeo.mininglittlemaid.task;

import com.github.lonelygeo.mininglittlemaid.config.Config;
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

public class MaidMineTorchPlaceTask extends MaidCheckRateTask {
    private static final int CHECK_RATE = 60;
    private static final String NO_TORCH_KEY = "message.mining_little_maid.no_torch";
    private static final int NOTIFY_MIN_INTERVAL = 200;
    private static final int NOTIFY_MIN_DIST_SQ = 16 * 16;
    private static final Logger LOGGER = LogManager.getLogger();
    private BlockPos lastPos = BlockPos.ZERO;
    private long lastPlaceTime;
    private long lastNotifyTime;
    private BlockPos lastNotifyPos = BlockPos.ZERO;

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

        long cooldown = Config.TORCH_COOLDOWN_TICKS.get();
        if (gameTime - lastPlaceTime < cooldown) {
            return;
        }

        BlockPos placePos = findPlaceableSurface(world, maid.blockPosition());
        if (placePos == null) {
            return;
        }

        if (!consumeTorch(maid)) {
            BlockPos currentPos = maid.blockPosition();
            if (gameTime - lastNotifyTime >= NOTIFY_MIN_INTERVAL
                    || currentPos.distSqr(lastNotifyPos) > NOTIFY_MIN_DIST_SQ) {
                if (maid.getOwner() instanceof ServerPlayer player) {
                    player.sendSystemMessage(Component.translatable(NO_TORCH_KEY));
                }
                lastNotifyTime = gameTime;
                lastNotifyPos = currentPos.immutable();
            }
            return;
        }

        world.setBlock(placePos, Blocks.TORCH.defaultBlockState(), 3);
        lastPlaceTime = gameTime;
        lastNotifyPos = BlockPos.ZERO;
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
