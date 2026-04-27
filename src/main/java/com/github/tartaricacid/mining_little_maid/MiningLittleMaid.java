package com.github.tartaricacid.mining_little_maid;

import com.github.tartaricacid.mining_little_maid.init.InitSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(MiningLittleMaid.MOD_ID)
public class MiningLittleMaid {
    public static final String MOD_ID = "mining_little_maid";

    public MiningLittleMaid(IEventBus modEventBus) {
        InitSounds.SOUNDS.register(modEventBus);
    }
}
