package com.github.lonelygeo.mininglittlemaid.inventory.container;

import com.github.tartaricacid.touhoulittlemaid.inventory.container.task.TaskConfigContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public class MiningTaskConfigContainer extends TaskConfigContainer {
    public static final MenuType<MiningTaskConfigContainer> TYPE =
            IMenuTypeExtension.create(MiningTaskConfigContainer::new);

    public MiningTaskConfigContainer(int containerId, Inventory inventory, int entityId) {
        super(TYPE, containerId, inventory, entityId);
    }

    public MiningTaskConfigContainer(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory, buf.readInt());
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return super.stillValid(player);
    }
}
