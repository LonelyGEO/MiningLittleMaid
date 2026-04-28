package com.github.lonelygeo.mininglittlemaid.compat;

import com.github.lonelygeo.mininglittlemaid.config.Config;
import com.github.tartaricacid.touhoulittlemaid.api.event.client.AddClothConfigEvent;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class MiningClothConfig {

    @SubscribeEvent
    public static void onAddClothConfig(AddClothConfigEvent event) {
        ConfigEntryBuilder entryBuilder = event.getEntryBuilder();
        ConfigCategory mining = event.getRoot().getOrCreateCategory(
                Component.translatable("config.mininglittlemaid.section.mining"));

        mining.addEntry(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.maxVeinSize"),
                        Config.MAX_VEIN_SIZE.get(), 2, 64)
                .setDefaultValue(8)
                .setSaveConsumer(val -> Config.MAX_VEIN_SIZE.set(val))
                .setTooltip(Component.literal("仅好感度等级 3 生效"))
                .build());

        mining.addEntry(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.minLightLevel"),
                        Config.MIN_LIGHT_LEVEL.get(), 0, 15)
                .setDefaultValue(7)
                .setSaveConsumer(val -> Config.MIN_LIGHT_LEVEL.set(val))
                .setTooltip(Component.literal("亮度低于此值时女仆放置火把"))
                .build());

        mining.addEntry(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.torchCooldown"),
                        Config.TORCH_COOLDOWN_TICKS.get(), 20, 600)
                .setDefaultValue(120)
                .setSaveConsumer(val -> Config.TORCH_COOLDOWN_TICKS.set(val))
                .setTooltip(Component.literal("火把放置冷却时间（tick）"))
                .build());

        mining.addEntry(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.combatReturnDelay"),
                        Config.COMBAT_RETURN_DELAY_TICKS.get(), 20, 600)
                .setDefaultValue(100)
                .setSaveConsumer(val -> Config.COMBAT_RETURN_DELAY_TICKS.set(val))
                .setTooltip(Component.literal("战斗结束后等多久切回采矿（tick）"))
                .build());

        mining.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("config.mininglittlemaid.enableDebugLog"),
                        Config.DEBUG_LOGGING.get())
                .setDefaultValue(false)
                .setSaveConsumer(val -> Config.DEBUG_LOGGING.set(val))
                .setTooltip(Component.literal("需重启生效"))
                .build());
    }
}
