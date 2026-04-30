# Mining Little Maid

为 [Touhou Little Maid](https://github.com/TartaricAcid/TouhouLittleMaid) 添加采矿任务的附属模组。

[![MC Version](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)](https://minecraft.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.186-orange)](https://neoforged.net)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

## 功能

| 功能 | 说明 | 好感度 |
|------|------|--------|
| 矿石搜索 | BFS 嗅探附近矿石，穿透石墙探测 | 无 |
| 洞顶探测 | BFS 无矿后垂直扫描头顶天花板裸露矿 | 无 |
| 矿脉连锁 | 同种矿石 BFS 连锁挖掘（上限可配） | Lv3+ |
| 耐久检查 | 镐子耐久不足自动从背包换备用镐 | Lv1+ |
| 背包满停止 | 背包已满时自动停止并通知 | 无 |
| 自动放火把 | 洞穴内亮度低于阈值自动放置火把 | 无 |
| 遇怪切战斗 | 检测怪物自动切战斗任务，结束后切回采矿 | Lv1+ |
| 遇怪说话 | 发现怪物时气泡+聊天栏提示，可对接 ELMAI | Lv1+ |
| 矿石种类开关 | 每只女仆独立配置要挖的矿石，采矿 Tab 操作 | 无 |
| 聊天栏开关 | 每只女仆独立控制是否发送系统消息 | 无 |
| 模组工具兼容 | Item Tag 驱动，支持第三方采矿工具 | 无 |
| 矿石白名单 | Block Tag 驱动，Datapack 增删可挖矿石 | 无 |
| 联动 API | MiningMessageEvent 供外部模组拦截消息 | 无 |
| 游戏内配置 | Cloth Config GUI，Mods 菜单直接调整所有参数 | — |

## 好感度等级

| 等级 | 好感度 | 水平嗅探 | 垂直嗅探 | 连锁挖掘 | 自动换镐 | 战斗切换 |
|------|--------|---------|---------|---------|---------|---------|
| 0 | 0-63 | 1 | 1 | ✗ | ✗ | ✗ |
| 1 | 64-191 | 2 | 2 | ✗ | ✓ | ✓ |
| 2 | 192-383 | 3 | 3 | ✗ | ✓ | ✓ |
| 3 | 384+ | 4 | 4 | ✓ | ✓ | ✓ |

> 每级水平/垂直嗅探半径可在 Config 中独立调整。

## 依赖

| 依赖 | 版本 |
|------|------|
| **Touhou Little Maid** | ≥ 1.5.2 |
| **Cloth Config API** | ≥ 15.0.140（可选，游戏内配置 GUI） |

## 配置项

所有参数通过「Mods → Touhou Little Maid → 配置 → 采矿」实时修改，或直接编辑 `config/mininglittlemaid-common.toml`。

### 通用

| 键 | 默认 | 范围 | 说明 |
|----|------|------|------|
| `maxVeinSize` | 9 | 1–64 | 矿脉连锁最大块数（Lv3+） |
| `minLightLevel` | 7 | 0–15 | 亮度低于此值放火把 |
| `enableDebugLog` | false | — | 实时输出采矿 AI 决策日志 |

### 冷却

| 键 | 默认 | Cloth Config | 说明 |
|----|------|-------------|------|
| `torchCooldownTicks` | 120 | 1–30 秒 | 火把放置冷却 |
| `torchNotifyCooldownTicks` | 12000 | 1–60 分 | 无火把通知冷却 |
| `combatReturnDelayTicks` | 100 | 1–30 秒 | 战斗结束切回采矿延迟 |
| `combatCooldownTicks` | 200 | 3–30 秒 | 切回采矿后战斗检测冷却 |
| `orePauseTicks` | 200 | 5–30 秒 | 不可达矿石后暂停搜索 |
| `oreAlertCooldownTicks` | 12000 | 1–60 分 | 同区域矿石通知冷却 |

### 频率

| 键 | 默认 | Cloth Config | 说明 |
|----|------|-------------|------|
| `combatCheckRate` | 60 | 1–10 秒 | 怪物检测间隔 |
| `durabilityCheckRate` | 60 | 1–10 秒 | 耐久检查间隔 |
| `inventoryCheckRate` | 60 | 1–10 秒 | 背包检查间隔 |
| `torchCheckRate` | 60 | 1–10 秒 | 火把检测间隔 |
| `bfsMaxDelay` | 120 | 2–30 秒 | BFS 搜索间隔 |
| `breakCheckRate` | 20 | 1–5 秒 | 挖掘检测间隔 |

### 范围

| 键 | 默认 | 范围(格) | 说明 |
|----|------|----------|------|
| `bfsSearchRadius` | 16 | 8–48 | BFS 水平搜索 |
| `bfsVerticalRange` | 16 | 4–32 | BFS 垂直搜索 |
| `combatSearchHorizontal` | 10 | 5–30 | 怪物搜索水平范围 |
| `combatSearchVertical` | 5 | 2–15 | 怪物搜索垂直范围 |
| `breakCloseEnoughHorizontal` | 3 | 1–6 | 水平接近判定 |
| `breakCloseEnoughAbove` | 3 | 2–5 | 向上高度差上限 |
| `breakCloseEnoughBelow` | 2 | 1–3 | 向下高度差下限 |
| `oreAlertMinDist` | 6 | 2–32 | 矿石通知同区域距离 |
| `adjacentOreSearchH` | 3 | 1–6 | 挖完后近邻水平搜索 |
| `adjacentOreSearchV` | 2 | 1–4 | 挖完后近邻垂直搜索 |
| `ceilingScanYStart` | 3 | 1–8 | 天花板扫描起始偏移 |
| `ceilingScanYEnd` | 16 | 8–24 | 天花板扫描终止偏移 |
| `ceilingScanHorizontal` | 1 | 0–2 | 天花板扫描水平半径 |
| `wanderRadiusH` | 12 | 4–32 | 闲逛水平范围 |
| `wanderRadiusV` | 4 | 2–16 | 闲逛垂直范围 |
| `torchSearchDepth` | 3 | 1–5 | 火把搜索放置面深度 |

### 感知范围

| 键 | 默认 | 范围 | 说明 |
|----|------|------|------|
| `sniffRadiusHorizontalLevel0` | 1 | 1–5 | Lv0 水平嗅探 |
| `sniffRadiusHorizontalLevel1` | 2 | 1–5 | Lv1 水平嗅探 |
| `sniffRadiusHorizontalLevel2` | 3 | 1–5 | Lv2 水平嗅探 |
| `sniffRadiusHorizontalLevel3` | 4 | 1–5 | Lv3 水平嗅探 |
| `sniffRadiusVerticalLevel0` | 1 | 1–5 | Lv0 垂直嗅探 |
| `sniffRadiusVerticalLevel1` | 2 | 1–5 | Lv1 垂直嗅探 |
| `sniffRadiusVerticalLevel2` | 3 | 1–5 | Lv2 垂直嗅探 |
| `sniffRadiusVerticalLevel3` | 4 | 1–5 | Lv3 垂直嗅探 |

## 联动 API

### MiningMessageEvent

供外部模组（如 EnhancedLittleMaidAI）拦截采矿消息，替换为 LLM 自然语言。

```java
@SubscribeEvent
public void onMiningMessage(MiningMessageEvent event) {
    event.setCanceled(true);
    // 获取原始数据：event.getMaid(), event.getType(), event.getOreName(), 
    // event.getOriginalBubbleText(), event.getOriginalSystemText(), event.getContext()
}
```

| 消息类型 | 触发场景 |
|---------|---------|
| `ORE_ABOVE` | 矿石在头顶 |
| `ORE_BELOW` | 矿石在脚下 |
| `ORE_UNREACHABLE` | 矿石无暴露面可达 |
| `INVENTORY_FULL` | 背包已满 |
| `NO_TORCH` | 缺少火把 |
| `COMBAT_DETECTED` | 发现怪物 |

Context Map 可选键：`target_pos`、`y_diff`、`block_state`、`kaomoji`、`monster_count`。

## Datapack 扩展

### 自定义可挖矿石（Block Tag）

`data/<namespace>/tags/block/mininglittlemaid/mineable_ores.json`

```json
{
    "replace": false,
    "values": [
        "my_mod:custom_ore_block"
    ]
}
```

### 自定义采矿工具（Item Tag）

`data/<namespace>/tags/item/mininglittlemaid/mining_tools.json`

```json
{
    "values": [
        "createaddition:drill"
    ]
}
```

### 自定义武器（Item Tag）

`data/<namespace>/tags/item/mininglittlemaid/weapons.json`

```json
{
    "values": [
        "my_mod:custom_sword"
    ]
}
```

## 构建

```bash
./gradlew.bat build
```

输出 JAR：`build/libs/mininglittlemaid-<version>.jar`

## 许可证

MIT License
