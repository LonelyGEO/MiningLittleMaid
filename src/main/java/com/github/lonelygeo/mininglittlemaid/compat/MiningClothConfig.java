package com.github.lonelygeo.mininglittlemaid.compat;

import com.github.lonelygeo.mininglittlemaid.MiningLittleMaid;
import com.github.lonelygeo.mininglittlemaid.config.Config;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.network.chat.Component;

public class MiningClothConfig {

    public static ConfigBuilder createConfigScreen() {
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(Component.literal("Mining Little Maid"));
        builder.setGlobalized(true);
        builder.setGlobalizedExpanded(true);

        ConfigCategory mining = builder.getOrCreateCategory(
                Component.translatable("config.mininglittlemaid.section.mining"));

        mining.addEntry(builder.entryBuilder()
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.maxVeinSize"),
                        Config.MAX_VEIN_SIZE.get(), 2, 64)
                .setDefaultValue(8)
                .setTooltip(Component.literal("仅好感度等级 3 生效"))
                .build());

        mining.addEntry(builder.entryBuilder()
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.minLightLevel"),
                        Config.MIN_LIGHT_LEVEL.get(), 0, 15)
                .setDefaultValue(7)
                .setTooltip(Component.literal("亮度低于此值时女仆放置火把"))
                .build());

        mining.addEntry(builder.entryBuilder()
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.torchCooldown"),
                        Config.TORCH_COOLDOWN_TICKS.get(), 20, 600)
                .setDefaultValue(120)
                .setTooltip(Component.literal("火把放置冷却时间（tick）"))
                .build());

        mining.addEntry(builder.entryBuilder()
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.combatReturnDelay"),
                        Config.COMBAT_RETURN_DELAY_TICKS.get(), 20, 600)
                .setDefaultValue(100)
                .setTooltip(Component.literal("战斗结束后等多久切回采矿（tick）"))
                .build());

        mining.addEntry(builder.entryBuilder()
                .startBooleanToggle(
                        Component.translatable("config.mininglittlemaid.enableDebugLog"),
                        Config.DEBUG_LOGGING.get())
                .setDefaultValue(false)
                .setTooltip(Component.literal("需重启生效"))
                .build());

        return builder;
    }
}
