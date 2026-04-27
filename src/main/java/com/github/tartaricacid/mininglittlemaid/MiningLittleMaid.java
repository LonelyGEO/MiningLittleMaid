package com.github.tartaricacid.mininglittlemaid;

import com.github.tartaricacid.mininglittlemaid.init.InitSounds;
import com.github.tartaricacid.mininglittlemaid.network.MiningChatNotifyToggleMessage;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(MiningLittleMaid.MOD_ID)
public class MiningLittleMaid {
    public static final String MOD_ID = "mining_little_maid";

    public MiningLittleMaid(IEventBus modEventBus) {
        InitSounds.SOUNDS.register(modEventBus);
        modEventBus.addListener(this::registerPayloadHandlers);
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(MiningChatNotifyToggleMessage.TYPE,
                MiningChatNotifyToggleMessage.STREAM_CODEC,
                MiningChatNotifyToggleMessage::handle);
    }
}
