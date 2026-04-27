# AGENTS.md

## Language policy

- 默认使用简体中文回答。
- 除非我明确要求英文，否则不要切换英文叙述。
- 代码、命令、报错、API 名称保持原文，不要强行翻译。
- 提问澄清时也使用中文。

## 1) Project snapshot

- **Name**: Mining Little Maid
- **Type**: Touhou Little Maid 附属模组（Addon mod）
- **Build system**: Gradle Wrapper (`gradlew`, `gradlew.bat`)
- **Language toolchain**: Java 21 (`build.gradle`)
- **Mod platform**: NeoForge (`net.neoforged.moddev` plugin 2.0.95)
- **Minecraft version**: 1.21.1
- **NeoForge version**: 21.1.186
- **Mod ID**: `mining_little_maid`
- **Mod version**: `1.0.0-neoforge+mc1.21.1`
- **Base package**: `com.github.tartaricacid.mining_little_maid`
- **Dependency**: Touhou Little Maid (`touhoulittlemaid-1.5.2-neoforge+mc1.21.1-all.jar`, in `libs/`)

## 2) Build / test / run commands

Run from repository root `G:\CursorProject\Mining-Little-Maid`.

- `./gradlew.bat clean`
- `./gradlew.bat build`
- `./gradlew.bat compileJava`
- `./gradlew.bat assemble`
- `./gradlew.bat runClient`
- `./gradlew.bat runServer`
- `./gradlew.bat tasks --all`

## 3) Architecture

### Entry point

`MiningAddonPlugin.java` — annotated with `@LittleMaidExtension`, implements `ILittleMaid`.
Registers the mining task via `addMaidTask(TaskManager)`.

### Task system

`TaskMining` implements `IFarmTask` (which extends `IMaidTask`):

| Method | Behavior |
|---|---|
| `canHarvest()` | Checks maid has a pickaxe + favor level allows mining the block |
| `harvest()` | Calls `maid.destroyBlock(pos)` |
| `createBrainTasks()` | Returns `MaidMineMoveTask` (priority 5) + `MaidMineBreakTask` (priority 6) |
| `onFunctionCallSwitch()` | Auto-equips pickaxe from backpack via `TaskEquipUtil` |
| `isSeed()` / `canPlant()` / `plant()` | Return false/no-op (mining doesn't plant) |
| `getConditionDescription()` | Shows "has_pickaxe" condition in GUI |
| `checkCropPosAbove()` | Returns false (no above-space check needed for mining) |
| `getCloseEnoughDist()` | Returns 2.0 (blocks) |
| `getMaidActionSummary()` | "Mine ores based on favor level" (for LLM tool calling) |

### Custom brain tasks

| Class | Extends | Role |
|---|---|---|
| `MaidMineMoveTask` | `MaidCheckRateTask` | BFS search for mineable ores in work area |
| `MaidMineBreakTask` | `Behavior<EntityMaid>` | Arrive → break block → erase TARGET_POS memory |

Search approach: Uses `MaidPathFindingBFS.find()` — BFS 从女仆位置向外扩散
搜索（驻留模式从 home 中心，跟随模式从女仆位置），找到第一个可挖掘矿石即停止。
无 `checkOwnerPos` 距离限制，跟随模式同样可挖矿。

### Favor gating

`MiningFavorGate` 根据女仆好感度等级决定嗅探半径（透过石头的探测距离）：

| Favor Level | Sniff Radius | 探测范围（相对可通行位置） |
|---|---|---|
| 0 (0-63) | 0 | 相邻 3×3×3（仅表面矿石） |
| 1 (64-191) | 1 | 5×5×5（穿透 1 格石头） |
| 2 (192-383) | 2 | 7×7×7（穿透 2 格石头） |
| 3 (384+) | 3 | 9×9×9（穿透 3 格石头） |

所有矿石类型在任何等级均可挖掘。`isMineableOre(BlockState)` 返回 true 对于任意矿石。

### Key APIs from Touhou Little Maid used

- `ILittleMaid` / `@LittleMaidExtension` — addon entry point
- `TaskManager.add(IMaidTask)` — register a new task
- `IFarmTask` — farm-like task interface (move → interact pattern)
- `MaidMoveToBlockTask` — BFS-driven block search with pathfinding
- `EntityMaid.destroyBlock(BlockPos)` — break block, auto-collect drops with enchant support
- `EntityMaid.canDestroyBlock(BlockPos)` — permission check
- `EntityMaid.getFavorabilityManager().getLevel()` — favor level (0-3)
- `TaskEquipUtil.tryEquipFromBackpack()` — auto-equip tool from backpack
- `ItemsUtil.isStackIn()` — check items in inventory
- `InitEntities.TARGET_POS` — `MemoryModuleType<PositionTracker>`, used as block target pointer
- `PositionTracker.currentPosition()` → `BlockPos.containing()` — convert target to BlockPos

### Translation keys

```
task.mining_little_maid.mining
task.mining_little_maid.mining.desc
task.mining_little_maid.mining.condition.has_pickaxe
```

Lang files: `src/main/resources/assets/mining_little_maid/lang/zh_cn.json` and `en_us.json`

### Dependency: Touhou Little Maid

The mod depends on the Touhou Little Maid jar at `libs/touhoulittlemaid-1.5.2-neoforge+mc1.21.1-all.jar`.
This is declared in `build.gradle` as:
```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
}
```

Note: TouhouLittleMaid's source code lives at `G:\CursorProject\Touhou Little Maid\src\`.

## 4) Code conventions

- Follow TouhouLittleMaid source code style (K&R braces, 4-space indent).
- Do NOT add unnecessary comments. Chinese comments are OK but keep them concise.
- Imports: split into project/local → third-party/Minecraft → Java → static.

## 5) Git workflow

- **Remote**: `https://github.com/LonelyGEO/MiningLittleMaid.git`
- **每个改动单独 commit**，不要多个不相关修改混在一个 commit 里。
- **不可逆操作必须询问**：包括但不限于 `git push --force`、`git reset --hard`、`git rebase`、分支删除、`--amend` 已推送的 commit。
- **遇到 SSH 权限错误、认证失败、远程冲突等 git 问题时，暂停操作并主动询问用户**。

## 6) Future improvements

- [ ] Add configurable ore whitelist via block tag
- [ ] Add particle effects / sound when mining
- [ ] Support vein mining (breaking connected ore blocks in one go)
- [ ] Add tool durability check before breaking
- [ ] Add "stop when inventory full" logic
- [ ] Add torch placement while mining (light up dark areas)
- [ ] Support the "Create" mod's drill tool as a pickaxe alternative
- [ ] Add custom ambient sound for mining (instead of reusing MAID_FARM sound)
