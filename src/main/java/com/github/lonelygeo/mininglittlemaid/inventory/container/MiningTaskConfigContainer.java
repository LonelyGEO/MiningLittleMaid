package com.github.lonelygeo.mininglittlemaid.inventory.container;

import com.github.tartaricacid.touhoulittlemaid.inventory.container.task.TaskConfigContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public class MiningTaskConfigContainer extends TaskConfigContainer {
    public static final MenuType<MiningTaskConfigContainer> TYPE =
            IMenuTypeExtension.create(MiningTaskConfigContainer::new);

    public MiningTaskConfigContainer(int containerId, Inventory inventory, int entityId) {
        super(TYPE, containerId, inventory, entityId);
    }

    private MiningTaskConfigContainer(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readInt());
    }
}
