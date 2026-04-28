package com.github.lonelygeo.mininglittlemaid.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.Logger;

public class Config {
    public static final ModConfigSpec SPEC;

    // [mining] 通用
    public static final ModConfigSpec.IntValue MAX_VEIN_SIZE;
    public static final ModConfigSpec.IntValue MIN_LIGHT_LEVEL;
    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING;

    // [mining.cooldown] 冷却
    public static final ModConfigSpec.IntValue TORCH_COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue TORCH_NOTIFY_COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue COMBAT_RETURN_DELAY_TICKS;
    public static final ModConfigSpec.IntValue COMBAT_COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue ORE_PAUSE_TICKS;
    public static final ModConfigSpec.IntValue ORE_ALERT_COOLDOWN_TICKS;

    // [mining.rate] 检测频率
    public static final ModConfigSpec.IntValue COMBAT_CHECK_RATE;
    public static final ModConfigSpec.IntValue DURABILITY_CHECK_RATE;
    public static final ModConfigSpec.IntValue INVENTORY_CHECK_RATE;
    public static final ModConfigSpec.IntValue TORCH_CHECK_RATE;
    public static final ModConfigSpec.IntValue BFS_MAX_DELAY;
    public static final ModConfigSpec.IntValue BREAK_CHECK_RATE;

    // [mining.range] 搜索范围
    public static final ModConfigSpec.IntValue BFS_SEARCH_RADIUS;
    public static final ModConfigSpec.IntValue BFS_VERTICAL_RANGE;
    public static final ModConfigSpec.IntValue COMBAT_SEARCH_HORIZONTAL;
    public static final ModConfigSpec.IntValue COMBAT_SEARCH_VERTICAL;
    public static final ModConfigSpec.IntValue BREAK_CLOSE_ENOUGH;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Mining Little Maid 配置");

        // [mining]
        builder.push("mining")
                .translation("config.mininglittlemaid.section.mining");

        MAX_VEIN_SIZE = builder
                .comment("矿脉连锁最大块数（仅好感度等级 3 生效）")
                .defineInRange("maxVeinSize", 8, 2, 64);

        MIN_LIGHT_LEVEL = builder
                .comment("亮度低于此值时女仆放置火把")
                .defineInRange("minLightLevel", 7, 0, 15);

        DEBUG_LOGGING = builder
                .comment("启用 Debug 日志输出（需重启生效）")
                .define("enableDebugLog", false);

        // [mining.cooldown]
        builder.push("cooldown")
                .translation("config.mininglittlemaid.section.cooldown");

        TORCH_COOLDOWN_TICKS = builder
                .comment("火把放置冷却时间")
                .defineInRange("torchCooldownTicks", 120, 20, 600);

        TORCH_NOTIFY_COOLDOWN_TICKS = builder
                .comment("无火把通知的冷却时间")
                .defineInRange("torchNotifyCooldownTicks", 6000, 200, 72000);

        COMBAT_RETURN_DELAY_TICKS = builder
                .comment("战斗结束后等多久切回采矿")
                .defineInRange("combatReturnDelayTicks", 100, 20, 600);

        COMBAT_COOLDOWN_TICKS = builder
                .comment("切换回采矿后的战斗检测冷却")
                .defineInRange("combatCooldownTicks", 200, 60, 600);

        ORE_PAUSE_TICKS = builder
                .comment("检测到不可达矿石后暂停搜索的时长")
                .defineInRange("orePauseTicks", 200, 100, 600);

        ORE_ALERT_COOLDOWN_TICKS = builder
                .comment("同一区域矿石通知的冷却时间")
                .defineInRange("oreAlertCooldownTicks", 12000, 2400, 72000);

        builder.pop();

        // [mining.rate]
        builder.push("rate")
                .translation("config.mininglittlemaid.section.rate");

        COMBAT_CHECK_RATE = builder
                .comment("战斗检测间隔")
                .defineInRange("combatCheckRate", 60, 20, 200);

        DURABILITY_CHECK_RATE = builder
                .comment("耐久检查间隔")
                .defineInRange("durabilityCheckRate", 60, 20, 200);

        INVENTORY_CHECK_RATE = builder
                .comment("背包检查间隔")
                .defineInRange("inventoryCheckRate", 60, 20, 200);

        TORCH_CHECK_RATE = builder
                .comment("火把检测间隔")
                .defineInRange("torchCheckRate", 60, 20, 200);

        BFS_MAX_DELAY = builder
                .comment("BFS 搜索的最大间隔")
                .defineInRange("bfsMaxDelay", 120, 40, 600);

        BREAK_CHECK_RATE = builder
                .comment("挖掘检测间隔")
                .defineInRange("breakCheckRate", 20, 10, 100);

        builder.pop();

        // [mining.range]
        builder.push("range")
                .translation("config.mininglittlemaid.section.range");

        BFS_SEARCH_RADIUS = builder
                .comment("BFS 矿石嗅探水平范围（格）")
                .defineInRange("bfsSearchRadius", 16, 8, 48);

        BFS_VERTICAL_RANGE = builder
                .comment("BFS 矿石嗅探垂直范围（格）")
                .defineInRange("bfsVerticalRange", 16, 4, 32);

        COMBAT_SEARCH_HORIZONTAL = builder
                .comment("怪物搜索水平半径（格）")
                .defineInRange("combatSearchHorizontal", 10, 5, 30);

        COMBAT_SEARCH_VERTICAL = builder
                .comment("怪物搜索垂直半径（格）")
                .defineInRange("combatSearchVertical", 5, 2, 15);

        BREAK_CLOSE_ENOUGH = builder
                .comment("女仆认为已到达目标的水平距离（格）")
                .defineInRange("breakCloseEnough", 3, 1, 6);

        builder.pop(3);

        SPEC = builder.build();
    }

    public static void debugLog(Logger logger, String message, Object... params) {
        if (DEBUG_LOGGING.get()) {
            logger.debug(message, params);
        }
    }
}
