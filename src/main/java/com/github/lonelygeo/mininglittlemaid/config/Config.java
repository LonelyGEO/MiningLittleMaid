package com.github.lonelygeo.mininglittlemaid.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MAX_VEIN_SIZE;
    public static final ModConfigSpec.IntValue MIN_LIGHT_LEVEL;
    public static final ModConfigSpec.IntValue TORCH_COOLDOWN_TICKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Mining Little Maid 配置");

        MAX_VEIN_SIZE = builder
                .comment("矿脉连锁最大块数（仅好感度等级 3 生效）")
                .defineInRange("maxVeinSize", 8, 2, 64);

        MIN_LIGHT_LEVEL = builder
                .comment("亮度低于此值时女仆放置火把")
                .defineInRange("minLightLevel", 7, 0, 15);

        TORCH_COOLDOWN_TICKS = builder
                .comment("火把放置冷却时间（tick，20 tick = 1 秒）")
                .defineInRange("torchCooldownTicks", 120, 20, 600);

        SPEC = builder.build();
    }
}
