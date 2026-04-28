package com.github.lonelygeo.mininglittlemaid;

import com.github.lonelygeo.mininglittlemaid.config.Config;
import com.github.lonelygeo.mininglittlemaid.init.InitSounds;
import com.github.lonelygeo.mininglittlemaid.inventory.container.MiningTaskConfigContainer;
import com.github.lonelygeo.mininglittlemaid.network.MiningChatNotifyToggleMessage;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(MiningLittleMaid.MOD_ID)
public class MiningLittleMaid {
    public static final String MOD_ID = "mining_little_maid";

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final Supplier<MenuType<MiningTaskConfigContainer>> MINING_TASK_CONFIG =
            MENU_TYPES.register("mining_task_config",
                    () -> IMenuTypeExtension.create(MiningTaskConfigContainer::new));

    public MiningLittleMaid(IEventBus modEventBus, ModContainer modContainer) {
        InitSounds.SOUNDS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        modEventBus.addListener(this::registerPayloadHandlers);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(MiningChatNotifyToggleMessage.TYPE,
                MiningChatNotifyToggleMessage.STREAM_CODEC,
                MiningChatNotifyToggleMessage::handle);
    }
}
