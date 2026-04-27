package com.github.lonelygeo.mininglittlemaid;

import com.github.lonelygeo.mininglittlemaid.config.Config;
import com.github.lonelygeo.mininglittlemaid.init.InitSounds;
import com.github.lonelygeo.mininglittlemaid.network.MiningChatNotifyToggleMessage;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(MiningLittleMaid.MOD_ID)
public class MiningLittleMaid {
    public static final String MOD_ID = "mining_little_maid";

    public MiningLittleMaid(IEventBus modEventBus, ModContainer modContainer) {
        InitSounds.SOUNDS.register(modEventBus);
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
