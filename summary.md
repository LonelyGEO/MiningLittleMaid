# Mining Little Maid - 工作进度总结

> 最后更新: 2026-04-28 | 版本: 0.1.0-neoforge+mc1.21.1

## 1) 项目基础

| 项 | 值 |
|---|---|
| 名称 | Mining Little Maid (采矿小女仆) |
| 类型 | Touhou Little Maid 附属模组 |
| MC 版本 | 1.21.1 |
| Mod 平台 | NeoForge 21.1.186 |
| Java | 21 |
| 构建工具 | Gradle Wrapper + `net.neoforged.moddev` 2.0.95 |
| Mod ID | `mininglittlemaid` |
| 包名 | `com.github.lonelygeo.mininglittlemaid` |
| 当前版本 | `0.1.0-neoforge+mc1.21.1` |
| 仓库 | https://github.com/LonelyGEO/MiningLittleMaid.git |
| 本体依赖 | `touhoulittlemaid-1.5.2-neoforge+mc1.21.1-all.jar` (放在 `libs/`) |
| 本体源码 | `G:\CursorProject\TouhouLittleMaid\src\` |

## 2) 源代码清单

### Java（9 个文件）

```
src/main/java/com/github/lonelygeo/mininglittlemaid/
├── MiningLittleMaid.java          @Mod 主入口，注册 SoundEvent + 网络包
├── MiningAddonPlugin.java         @LittleMaidExtension 扩展入口，注册 TaskMining
├── init/
│   └── InitSounds.java            MAID_MINING SoundEvent（maid.mode.mining）
├── task/
│   ├── TaskMining.java            IFarmTask 实现，挖矿任务定义
│   ├── MaidMineMoveTask.java      扩展 MaidCheckRateTask，BFS 搜索矿石 + 随机游走
│   ├── MaidMineBreakTask.java     扩展 Behavior<EntityMaid>，挖掘 + 连锁 + 垂直提示
│   └── MiningFavorGate.java       好感度嗅探半径门控
├── network/
│   └── MiningChatNotifyToggleMessage.java  聊天提示开关（record + CustomPacketPayload）
└── event/
    └── ClientGuiEventHandler.java  设置 tab 内挖矿提示 Checkbox
```

### 资源（4 个文件）

```
src/main/resources/
├── META-INF/neoforge.mods.toml
└── assets/mining_little_maid/
    ├── sounds.json                 maid.mode.mining → base mod 静音 OGG
    └── lang/
        ├── zh_cn.json
        └── en_us.json
```

## 3) 已实现功能（按 Commit 顺序）

### 基础架构
- `@Mod` + `@LittleMaidExtension` 双入口，正确注册任务到 TouhouLittleMaid
- 独立 `MAID_MINING` SoundEvent（`maid.mode.mining`），音效包可定制

### 采矿 AI
- **纯 BFS 搜索引擎**（绕过 base mod 的 `checkOwnerPos` 8 格限制）
- BFS 从女仆位置向外扩散，检查可通行位置相邻方块是否为矿石
- **连锁挖矿**：挖完一块后在 7×5×7 范围内寻找下一块
- **无矿随机游走**：BFS 找不到矿时在主人周围 12×4×12 随机走动

### 好感度系统（嗅探穿透）
| 好感度 | 好感度值 | 嗅探半径 | 每节点扫描 |
|---|---|---|---|
| 0 | 0-63 | 1 | 3×3×3 (27格) |
| 1 | 64-191 | 2 | 5×5×5 (125格) |
| 2 | 192-383 | 2 | 5×5×5 (125格) |
| 3 | 384+ | 3 | 7×7×7 (343格) |

所有矿石类型在任何等级均可挖掘，等级只影响探测范围。

### 模式限制
- 挖矿任务**仅在跟随模式下启用**
- 驻留模式下任务按钮灰显，不可切换
- 跟随模式下 BFS 以女仆位置为中心搜索（16 格 BFS 半径）

### 垂直矿石提示
- 矿在头顶/脚下 >2 格 → **不挖掘**，触发通知：
  - 女仆头顶绿色开心粒子（`HAPPY_VILLAGER`）
  - 文字气泡「头顶或脚下发现了矿石！」（`ChatBubbleManager`）
  - 聊天栏消息（可开关）
- 矿在同一水平面（≤2 格）→ 正常挖掘

### 聊天提示开关
- 设置 tab（`MaidConfigContainerGui`）内有「挖矿提示」Checkbox
- 状态存储在女仆 NBT 持久数据（`getPersistentData()`），默认开启
- 客户端点击 → `MiningChatNotifyToggleMessage` 网络包 → 服务端切换

### Bug 修复
- 垂直搜索漏层：`VERTICAL_SEARCH_START` 从 -8 改为 0
- BFS 只搜索空气块：改为检查节点邻接方向方块
- `WALK_TARGET closeEnoughDist=0` 导致女仆永远走不到固体矿石：改为 2

## 4) 翻译键

```
task.mininglittlemaid.mining                         采矿 / Mining
task.mininglittlemaid.mining.desc                    任务描述（嗅探范围说明）
task.mininglittlemaid.mining.condition.has_pickaxe   背包内有镐子
subtitle.mining_little_maid.maid.mode.mining         女仆：采矿 / Maid: Mining
message.mining_little_maid.ore_above_below           头顶或脚下发现了矿石！
gui.mininglittlemaid.chat_notify                     挖矿提示 / Mining Alert
```

## 5) 关键 API 调用（TouhouLittleMaid 本体）

| API | 用途 |
|---|---|
| `ILittleMaid` / `@LittleMaidExtension` | Addon 入口 |
| `TaskManager.add(IMaidTask)` | 注册任务 |
| `IFarmTask` | 任务接口 |
| `MaidCheckRateTask` | 限流父类 |
| `MaidPathFindingBFS.find(Predicate)` | BFS 矿石搜索 |
| `EntityMaid.destroyBlock(BlockPos)` | 挖掘方块 |
| `EntityMaid.getFavorabilityManager().getLevel()` | 好感度等级 |
| `EntityMaid.getChatBubbleManager().addTextChatBubble()` | 文字气泡 |
| `EntityMaid.getPersistentData()` | NBT 持久数据 |
| `TaskEquipUtil.tryEquipFromBackpack()` | 自动装备镐子 |
| `InitEntities.TARGET_POS` | 目标位置记忆 |
| `MaidContainerGuiEvent.Init` | GUI 按钮注入事件 |
| `MaidConfigContainerGui` | 设置 tab GUI |

## 6) 测试清单

| # | 测试项 | 预期 |
|---|---|---|
| 1 | 驻留模式 → 采矿任务灰显 | 不可切换 |
| 2 | 跟随模式 → 切换到采矿 | 任务激活 |
| 3 | 背包有镐子 | 自动装备 |
| 4 | 带女仆进矿洞 | 搜索周围矿石并走过去 |
| 5 | 走到矿石旁 | 挖掘并拾取 |
| 6 | 挖完一块旁边还有 | 连锁挖下一块 |
| 7 | 无矿环境 | 主人周围随机游走 |
| 8 | 矿在头顶/脚下 >2 格 | 粒子+气泡+聊天提示，不挖 |
| 9 | 设置 tab → 挖矿提示勾选框 | 默认勾选，点击切换 |
| 10 | 取消勾选后矿在头顶 | 无聊天栏消息（仍有粒子） |

## 7) 运行命令

```bash
# 构建
./gradlew.bat build

# 编译
./gradlew.bat compileJava

# 启动测试客户端
./gradlew.bat runClient
```
