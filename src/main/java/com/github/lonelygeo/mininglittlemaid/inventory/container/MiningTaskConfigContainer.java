package com.github.lonelygeo.mininglittlemaid.inventory.container;

import com.github.lonelygeo.mininglittlemaid.MiningLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.task.TaskConfigContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class MiningTaskConfigContainer extends TaskConfigContainer {
    public MiningTaskConfigContainer(int containerId, Inventory inventory, int entityId) {
        super(MiningLittleMaid.MINING_TASK_CONFIG.get(), containerId, inventory, entityId);
    }

    public MiningTaskConfigContainer(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory, buf.readInt());
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return super.stillValid(player);
    }
}
