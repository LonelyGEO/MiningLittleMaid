package com.github.lonelygeo.mininglittlemaid.compat;

import com.github.lonelygeo.mininglittlemaid.config.Config;
import com.github.tartaricacid.touhoulittlemaid.api.event.client.AddClothConfigEvent;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

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
                        Config.MAX_VEIN_SIZE.get(), 1, 64)
                .setDefaultValue(9)
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
                .startBooleanToggle(
                        Component.translatable("config.mininglittlemaid.enableDebugLog"),
                        Config.DEBUG_LOGGING.get())
                .setDefaultValue(false)
                .setSaveConsumer(val -> Config.DEBUG_LOGGING.set(val))
                .setTooltip(Component.literal("启用后实时输出采矿 AI 决策日志"))
                .build());

        // 采矿 - 冷却
        SubCategoryBuilder cooldownSub = entryBuilder.startSubCategory(
                Component.translatable("config.mininglittlemaid.section.cooldown"));
        cooldownSub.setExpanded(false);

        cooldownSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.torchCooldown"),
                        Config.TORCH_COOLDOWN_TICKS.get() / 20, 1, 30)
                .setDefaultValue(6)
                .setSaveConsumer(val -> Config.TORCH_COOLDOWN_TICKS.set(val * 20))
                .setTooltip(Component.literal("火把放置冷却时间（秒）"))
                .build());

        cooldownSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.torchNotifyCooldownTicks"),
                        Config.TORCH_NOTIFY_COOLDOWN_TICKS.get() / 1200, 1, 60)
                .setDefaultValue(10)
                .setSaveConsumer(val -> Config.TORCH_NOTIFY_COOLDOWN_TICKS.set(val * 1200))
                .setTooltip(Component.literal("无火把通知的冷却时间（分钟）"))
                .build());

        cooldownSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.combatReturnDelay"),
                        Config.COMBAT_RETURN_DELAY_TICKS.get() / 20, 1, 30)
                .setDefaultValue(5)
                .setSaveConsumer(val -> Config.COMBAT_RETURN_DELAY_TICKS.set(val * 20))
                .setTooltip(Component.literal("战斗结束后等多久切回采矿（秒）"))
                .build());

        cooldownSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.combatCooldownTicks"),
                        Config.COMBAT_COOLDOWN_TICKS.get() / 20, 3, 30)
                .setDefaultValue(10)
                .setSaveConsumer(val -> Config.COMBAT_COOLDOWN_TICKS.set(val * 20))
                .setTooltip(Component.literal("切回采矿后需要等待多久才能再次触发战斗检测（秒）"))
                .build());

        cooldownSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.orePauseTicks"),
                        Config.ORE_PAUSE_TICKS.get() / 20, 5, 30)
                .setDefaultValue(10)
                .setSaveConsumer(val -> Config.ORE_PAUSE_TICKS.set(val * 20))
                .setTooltip(Component.literal("检测到不可达矿石后暂停搜索的时长（秒）"))
                .build());

        cooldownSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.oreAlertCooldownTicks"),
                        Config.ORE_ALERT_COOLDOWN_TICKS.get() / 1200, 1, 60)
                .setDefaultValue(10)
                .setSaveConsumer(val -> Config.ORE_ALERT_COOLDOWN_TICKS.set(val * 1200))
                .setTooltip(Component.literal("同一区域矿石通知的冷却时间（分钟）"))
                .build());

        mining.addEntry(cooldownSub.build());

        // 采矿 - 频率
        SubCategoryBuilder rateSub = entryBuilder.startSubCategory(
                Component.translatable("config.mininglittlemaid.section.rate"));
        rateSub.setExpanded(false);

        rateSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.combatCheckRate"),
                        Config.COMBAT_CHECK_RATE.get() / 20, 1, 10)
                .setDefaultValue(3)
                .setSaveConsumer(val -> Config.COMBAT_CHECK_RATE.set(val * 20))
                .setTooltip(Component.literal("女仆检测周围怪物的间隔。值越小反应越快，但实体查询是 CPU 大户（秒）"))
                .build());

        rateSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.durabilityCheckRate"),
                        Config.DURABILITY_CHECK_RATE.get() / 20, 1, 10)
                .setDefaultValue(3)
                .setSaveConsumer(val -> Config.DURABILITY_CHECK_RATE.set(val * 20))
                .setTooltip(Component.literal("检查主手工具耐久并自动换镐的间隔（秒）"))
                .build());

        rateSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.inventoryCheckRate"),
                        Config.INVENTORY_CHECK_RATE.get() / 20, 1, 10)
                .setDefaultValue(3)
                .setSaveConsumer(val -> Config.INVENTORY_CHECK_RATE.set(val * 20))
                .setTooltip(Component.literal("检查背包是否已满的间隔（秒）"))
                .build());

        rateSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.torchCheckRate"),
                        Config.TORCH_CHECK_RATE.get() / 20, 1, 10)
                .setDefaultValue(3)
                .setSaveConsumer(val -> Config.TORCH_CHECK_RATE.set(val * 20))
                .setTooltip(Component.literal("检查脚下亮度并放置火把的间隔（秒）"))
                .build());

        rateSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.bfsMaxDelay"),
                        Config.BFS_MAX_DELAY.get() / 20, 2, 30)
                .setDefaultValue(6)
                .setSaveConsumer(val -> Config.BFS_MAX_DELAY.set(val * 20))
                .setTooltip(Component.literal("BFS 无果后的等待间隔。值越小两次搜索间停顿越短，搜得越勤（秒）"))
                .build());

        rateSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.breakCheckRate"),
                        Config.BREAK_CHECK_RATE.get() / 20, 1, 5)
                .setDefaultValue(1)
                .setSaveConsumer(val -> Config.BREAK_CHECK_RATE.set(val * 20))
                .setTooltip(Component.literal("到达目标后尝试挖掘的检查频率。值越小越「粘人」（秒）"))
                .build());

        mining.addEntry(rateSub.build());

        // 采矿 - 范围
        SubCategoryBuilder rangeSub = entryBuilder.startSubCategory(
                Component.translatable("config.mininglittlemaid.section.range"));
        rangeSub.setExpanded(false);

        rangeSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.bfsSearchRadius"),
                        Config.BFS_SEARCH_RADIUS.get(), 8, 48)
                .setDefaultValue(16)
                .setSaveConsumer(val -> Config.BFS_SEARCH_RADIUS.set(val))
                .setTooltip(Component.literal("矿石嗅探的水平范围（格）。上限越大越耗 CPU，服务器建议 24 以下"))
                .build());

        rangeSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.bfsVerticalRange"),
                        Config.BFS_VERTICAL_RANGE.get(), 4, 32)
                .setDefaultValue(16)
                .setSaveConsumer(val -> Config.BFS_VERTICAL_RANGE.set(val))
                .setTooltip(Component.literal("矿石嗅探的垂直范围（格）。调大可探测更高/更深的矿"))
                .build());

        rangeSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.combatSearchHorizontal"),
                        Config.COMBAT_SEARCH_HORIZONTAL.get(), 5, 30)
                .setDefaultValue(10)
                .setSaveConsumer(val -> Config.COMBAT_SEARCH_HORIZONTAL.set(val))
                .setTooltip(Component.literal("战斗检测的水平范围（格）。越大越早发现远处的怪物，但开销也越大"))
                .build());

        rangeSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.combatSearchVertical"),
                        Config.COMBAT_SEARCH_VERTICAL.get(), 2, 15)
                .setDefaultValue(5)
                .setSaveConsumer(val -> Config.COMBAT_SEARCH_VERTICAL.set(val))
                .setTooltip(Component.literal("战斗检测的垂直范围（格）"))
                .build());

        rangeSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.breakCloseEnoughHorizontal"),
                        Config.BREAK_CLOSE_ENOUGH_H.get(), 1, 6)
                .setDefaultValue(3)
                .setSaveConsumer(val -> Config.BREAK_CLOSE_ENOUGH_H.set(val))
                .setTooltip(Component.literal("女仆认为水平面已接近目标的距离（格）。值越大越容易远程采矿"))
                .build());

        rangeSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.breakCloseEnoughAbove"),
                        Config.BREAK_CLOSE_ENOUGH_ABOVE.get(), 2, 5)
                .setDefaultValue(3)
                .setSaveConsumer(val -> Config.BREAK_CLOSE_ENOUGH_ABOVE.set(val))
                .setTooltip(Component.literal("女仆允许挖掘的头顶高度差上限（格）。超过此差会通知玩家"))
                .build());

        rangeSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.breakCloseEnoughBelow"),
                        Config.BREAK_CLOSE_ENOUGH_BELOW.get(), 1, 3)
                .setDefaultValue(2)
                .setSaveConsumer(val -> Config.BREAK_CLOSE_ENOUGH_BELOW.set(val))
                .setTooltip(Component.literal("女仆允许挖掘的脚下高度差下限（格）。超过此差会通知玩家"))
                .build());

        rangeSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.oreAlertMinDist"),
                        Config.ORE_ALERT_MIN_DIST.get(), 2, 32)
                .setDefaultValue(6)
                .setSaveConsumer(val -> Config.ORE_ALERT_MIN_DIST.set(val))
                .setTooltip(Component.literal("两次矿石通知视为同区域的距离（格）。超过此距离必定重新提示"))
                .build());

        mining.addEntry(rangeSub.build());

        // 采矿 - 感知范围
        SubCategoryBuilder perceptionSub = entryBuilder.startSubCategory(
                Component.translatable("config.mininglittlemaid.section.perception"));
        perceptionSub.setExpanded(false);

        perceptionSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.sniffRadiusHorizontalLevel0"),
                        Config.SNIFF_RADIUS_H_LEVEL_0.get(), 1, 5)
                .setDefaultValue(1)
                .setSaveConsumer(val -> Config.SNIFF_RADIUS_H_LEVEL_0.set(val))
                .setTooltip(Component.literal("好感度 0 级（0-63）水平嗅探半径（格）"))
                .build());

        perceptionSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.sniffRadiusHorizontalLevel1"),
                        Config.SNIFF_RADIUS_H_LEVEL_1.get(), 1, 5)
                .setDefaultValue(2)
                .setSaveConsumer(val -> Config.SNIFF_RADIUS_H_LEVEL_1.set(val))
                .setTooltip(Component.literal("好感度 1 级（64-191）水平嗅探半径（格）"))
                .build());

        perceptionSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.sniffRadiusHorizontalLevel2"),
                        Config.SNIFF_RADIUS_H_LEVEL_2.get(), 1, 5)
                .setDefaultValue(3)
                .setSaveConsumer(val -> Config.SNIFF_RADIUS_H_LEVEL_2.set(val))
                .setTooltip(Component.literal("好感度 2 级（192-383）水平嗅探半径（格）"))
                .build());

        perceptionSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.sniffRadiusHorizontalLevel3"),
                        Config.SNIFF_RADIUS_H_LEVEL_3.get(), 1, 5)
                .setDefaultValue(4)
                .setSaveConsumer(val -> Config.SNIFF_RADIUS_H_LEVEL_3.set(val))
                .setTooltip(Component.literal("好感度 3 级（384+）水平嗅探半径（格）"))
                .build());

        perceptionSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.sniffRadiusVerticalLevel0"),
                        Config.SNIFF_RADIUS_V_LEVEL_0.get(), 1, 5)
                .setDefaultValue(1)
                .setSaveConsumer(val -> Config.SNIFF_RADIUS_V_LEVEL_0.set(val))
                .setTooltip(Component.literal("好感度 0 级（0-63）垂直嗅探半径（格）"))
                .build());

        perceptionSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.sniffRadiusVerticalLevel1"),
                        Config.SNIFF_RADIUS_V_LEVEL_1.get(), 1, 5)
                .setDefaultValue(2)
                .setSaveConsumer(val -> Config.SNIFF_RADIUS_V_LEVEL_1.set(val))
                .setTooltip(Component.literal("好感度 1 级（64-191）垂直嗅探半径（格）"))
                .build());

        perceptionSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.sniffRadiusVerticalLevel2"),
                        Config.SNIFF_RADIUS_V_LEVEL_2.get(), 1, 5)
                .setDefaultValue(3)
                .setSaveConsumer(val -> Config.SNIFF_RADIUS_V_LEVEL_2.set(val))
                .setTooltip(Component.literal("好感度 2 级（192-383）垂直嗅探半径（格）"))
                .build());

        perceptionSub.add(entryBuilder
                .startIntSlider(
                        Component.translatable("config.mininglittlemaid.sniffRadiusVerticalLevel3"),
                        Config.SNIFF_RADIUS_V_LEVEL_3.get(), 1, 5)
                .setDefaultValue(4)
                .setSaveConsumer(val -> Config.SNIFF_RADIUS_V_LEVEL_3.set(val))
                .setTooltip(Component.literal("好感度 3 级（384+）垂直嗅探半径（格）"))
                .build());

        mining.addEntry(perceptionSub.build());
    }
}
