# Mining Little Maid

为 [Touhou Little Maid](https://github.com/TartaricAcid/TouhouLittleMaid) 添加采矿任务的附属模组。

[![MC Version](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)](https://minecraft.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.186-orange)](https://neoforged.net)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

## 功能

| 功能 | 说明 | 好感度要求 |
|------|------|-----------|
| 矿石搜索 | BFS 扫描附近矿石并自动移动到可挖掘位置 | 无 |
| 矿脉连锁 | 同种矿石连锁挖掘（上限可配） | Lv3 (384+) |
| 耐久检查 | 镐子耐久不足自动换备用镐，无备镐取消任务 | Lv1+ (64+) |
| 背包满停止 | 背包已满时自动停止并通知 | 无 |
| 自动放火把 | 亮度低于阈值时自动放置火把 | 无 |
| 遇怪切战斗 | 检测怪物自动切换战斗，结束后切回采矿 | Lv1+ (64+) |
| 模组工具兼容 | 通过 Item Tag 支持第三方采矿工具 | 无 |
| 矿石白名单 | 通过 Block Tag 自定义可挖矿石（支持 Datapack） | 无 |
| 游戏内配置 | Cloth Config GUI，Mods 菜单直接调整参数 | — |

## 好感度等级

| 等级 | 好感度 | 嗅探半径 | 探测范围 | 连锁挖掘 | 自动换镐 | 战斗切换 |
|------|--------|---------|---------|---------|---------|---------|
| 0 | 0-63 | 1 | 3×3×3 | ✗ | ✗ | ✗ |
| 1 | 64-191 | 2 | 5×5×5 | ✗ | ✓ | ✓ |
| 2 | 192-383 | 2 | 5×5×5 | ✗ | ✓ | ✓ |
| 3 | 384+ | 3 | 7×7×7 | ✓ | ✓ | ✓ |

## 依赖

| 依赖 | 说明 |
|------|------|
| **Touhou Little Maid** (≥1.5.2) | 主体模组 |
| **Cloth Config API** (≥15.0.140) | 游戏内配置 GUI（可选） |

## 可配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `maxVeinSize` | 8 (2-64) | 矿脉连锁最大块数 |
| `minLightLevel` | 7 (0-15) | 自动放火把的亮度阈值 |
| `torchCooldown` | 120 ticks | 火把放置冷却时间 |
| `combatReturnDelay` | 100 ticks | 战斗结束切回采矿的延迟 |

## Datapack 扩展

### 自定义可挖矿石（Block Tag）

`data/<namespace>/tags/block/mining_tools.json`

```json
{
    "values": [
        "my_mod:custom_ore_block"
    ]
}
```

### 自定义采矿工具（Item Tag）

`data/<namespace>/tags/item/mining_tools.json`

```json
{
    "values": [
        "createaddition:drill"
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
