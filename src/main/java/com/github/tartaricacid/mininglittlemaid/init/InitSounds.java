package com.github.tartaricacid.mininglittlemaid.init;

import com.github.tartaricacid.mininglittlemaid.MiningLittleMaid;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class InitSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, MiningLittleMaid.MOD_ID);

    public static final Supplier<SoundEvent> MAID_MINING = SOUNDS.register("maid.mode.mining",
            () -> SoundEvent.createFixedRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(MiningLittleMaid.MOD_ID, "maid.mode.mining"), 16.0F));

    private InitSounds() {
    }
}
