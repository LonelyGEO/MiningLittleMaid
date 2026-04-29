# AGENTS.md

## Language policy

- 默认使用简体中文回答。
- 除非我明确要求英文，否则不要切换英文叙述。
- 代码、命令、报错、API 名称保持原文，不要强行翻译。
- 提问澄清时也使用中文。

Repository-specific guidance for coding agents working in `Mining-Little-Maid-1.21`.
All items below were verified against the current repository contents.

## 1) Project snapshot

- **Name**: Mining Little Maid 1.21
- **Type**: Touhou Little Maid 附属模组（Addon mod）
- Build system: **Gradle Wrapper** (`gradlew`, `gradlew.bat`)
- Language toolchain: **Java 21** (`build.gradle`)
- Mod platform: **NeoForge** (`net.neoforged.moddev` plugin 2.0.95)
- **Minecraft 1.21.1** / **NeoForge 21.1.186**
- **Mod ID**: `mininglittlemaid`
- **Base package**: `com.github.lonelygeo.mininglittlemaid`
- **Dependency**: Touhou Little Maid (`libs/touhoulittlemaid-1.5.2-neoforge+mc1.21.1-all.jar`)
- **Cloth Config API**: `me.shedaniel.cloth:cloth-config-neoforge:15.0.140`（游戏内配置 GUI）
- **Parent mod repo**: `https://github.com/TartaricAcid/TouhouLittleMaid`（API 查考）
- **联动模组仓库**：`https://github.com/LonelyGEO/EnhancedLittleMaidAI.git`

## 2) Cursor/Copilot rule files

- `.cursor/rules/`: not found
- `.cursorrules`: not found
- `.github/copilot-instructions.md`: not found

So there are no extra assistant policy files to inherit; follow existing code patterns.

## 3) Build / test / run commands

Run commands from repository root.
Use `.bat` on Windows and non-`.bat` equivalents on macOS/Linux.

### Core commands

- `./gradlew.bat clean`
- `./gradlew.bat build`
- `./gradlew.bat check`
- `./gradlew.bat test`
- `./gradlew.bat assemble`

### NeoForge dev commands

- `./gradlew.bat runClient`
- `./gradlew.bat runServer`
- `./gradlew.bat runGameTestServer`
- `./gradlew.bat runData`

> **runClient 超时说明**：`runClient` 会阻塞等待游戏窗口关闭（非短暂命令）。Agent 执行时至少用 `timeout=600000`（10分钟），确保用户在游戏内有足够操作时间完成交互测试。

### Task discovery

- `./gradlew.bat tasks --all`
- `./gradlew.bat help --task test`

### Single-test commands (important)

Gradle test filtering is supported via `--tests`.

- Single class:
  - `./gradlew.bat test --tests "com.github.lonelygeo.mininglittlemaid.ExampleTest"`
- Single method:
  - `./gradlew.bat test --tests "com.github.lonelygeo.mininglittlemaid.ExampleTest.shouldDoThing"`
- Method wildcard:
  - `./gradlew.bat test --tests "*ExampleTest.should*"`

Current state note: `src/test/java` is not present right now.

## 4) Lint/format reality

- No Spotless config found.
- No Checkstyle config found.
- No `.editorconfig` found.
- No dedicated `lint` Gradle task.
- Use `check` as verification umbrella task.

Practical rule: avoid style churn; keep formatting aligned with nearby code.

## 5) Style conventions from source code

### Formatting

- 4-space indentation.
- K&R braces.
- Blank lines between logical blocks.
- Wrapped long builder/fluent/record declarations.

### Imports

Observed grouping pattern:
1) project/local packages
2) third-party + Minecraft/NeoForge
3) `java.*`/`javax.*`
4) static imports last

Avoid reordering imports unless required by your edit.

### Naming

- Types (`class`, `record`, `enum`): `PascalCase`
- Methods/fields/locals/params: `camelCase`
- Constants (`static final`): `UPPER_SNAKE_CASE`
- Packages: lowercase, feature-oriented

### Types and modeling

- Packet payloads often use Java `record`.
- Packet classes typically expose:
  - `public static final Type<...> TYPE`
  - `public static final StreamCodec<...> STREAM_CODEC`
- Prefer explicit, readable types in shared/core code.

### Nullability

- Some packages declare defaults in `package-info.java`:
  - `@ParametersAreNonnullByDefault`
  - `@MethodsReturnNonnullByDefault`
- Use `@Nullable` explicitly where null is valid.
- Prefer guard clauses + early returns for invalid/null state.

### Error handling

- Catch specific exception types (`IOException`, parse exceptions, etc.).
- Log with useful context; do not silently swallow broadly.
- If intentionally ignoring exceptions, keep scope narrow and comment why.

### Logging

- Main logger pattern:
  - `public static final Logger LOGGER = LogManager.getLogger(MOD_ID);`
- Use parameterized logging (`{}` placeholders).
- Marker-based logging appears in resource-loading paths.

### Comments

- Comments are concise and intent-focused.
- Chinese comments are common; keep local language/style consistent.
- Do not add obvious comments that restate code.

## 6) Architecture hints

### Entry points

- `MiningLittleMaid` — `@Mod` 主类，注册音效、网络包、MenuType（任务配置 Tab）、IConfigScreenFactory（Cloth Config GUI）
- `MiningAddonPlugin` — `@LittleMaidExtension` 扩展入口，注册 `TaskMining`

### Package roles

| 包 | 职责 |
|---|---|
| `task.*` | 任务定义 + AI 行为（TaskMining, MaidMineMoveTask, MaidMineBreakTask, MiningFavorGate 等） |
| `init.*` | 注册（InitSounds 音效 DeferredRegister） |
| `network.*` | 网络包（MiningChatNotifyToggleMessage，record 类型） |
| `event.*` | 事件订阅（MaidMineCombatEventHandler，战斗结束切回采矿） |
| `config.*` | Mod 配置（Config.java，NeoForge Common Config） |
| `client.*` | 客户端（MiningLittleMaidClient 屏幕注册，gui 子包含 MiningTaskConfigGui） |
| `inventory.container.*` | 容器（MiningTaskConfigContainer，任务配置 Tab） |
| `compat.*` | Cloth Config 兼容（MiningClothConfig，游戏内配置 GUI） |
| `docs/` | 技术文档（config-system.md） |

### Task system

`TaskMining` implements `IFarmTask` (TouhouLittleMaid 的农场任务接口):

- `canHarvest()` — 检查镐子 + 矿石类型
- `harvest()` — 调用 `maid.destroyBlock(pos)`
- `createBrainTasks()` — 返回 6 个 AI: 4 个优先级 4 监控任务（耐久/背包/火把/战斗） + 优先级 5 移动 + 优先级 6 挖掘
- `onFunctionCallSwitch()` — 切换任务时自动从背包装备镐子
- `getTaskConfigGuiProvider()` — 返回采矿配置 Tab 的 MenuProvider

### AI behaviors

| 类 | 父类 | 优先级 | 职责 |
|---|---|---|---|
| `MaidMineDurabilityCheckTask` | `MaidCheckRateTask` | 4 | 耐久预检 + 自动换镐（好感 Lv1+） |
| `MaidMineInventoryCheckTask` | `MaidCheckRateTask` | 4 | 背包满时停止 + 通知 |
| `MaidMineTorchPlaceTask` | `MaidCheckRateTask` | 4 | 暗处自动放置火把 |
| `MaidMineCombatCheckTask` | `MaidCheckRateTask` | 4 | 遇怪自动切换战斗（好感 Lv1+） |
| `MaidMineMoveTask` | `MaidCheckRateTask` | 5 | BFS 搜索矿石（嗅探半径由好感度决定），找不到时随机跟随主人 |
| `MaidMineBreakTask` | `Behavior<EntityMaid>` | 6 | 到达 → 高度差检测 → 挖掘 → 矿脉连锁 → 近邻搜索 |

### Favor gating (MiningFavorGate)

好感度等级影响嗅探半径（透过石头的探测距离），不影响可挖掘矿石类型：

| 等级 | 好感度范围 | 嗅探半径 | 探测范围 |
|------|-----------|---------|---------|
| 0 | 0-63 | 1 | 3×3×3 |
| 1 | 64-191 | 2 | 5×5×5 |
| 2 | 192-383 | 2 | 5×5×5 |
| 3 | 384+ | 3 | 7×7×7 |

### Network

- `MiningChatNotifyToggleMessage` — `record` 类，实现 `CustomPacketPayload`
- 携带 `maidId: int`，切换女仆的聊天提示开关（playToServer）

### Translation keys

```
task.mininglittlemaid.mining
task.mininglittlemaid.mining.desc
task.mininglittlemaid.mining.condition.has_pickaxe
task.mininglittlemaid.mining.config
subtitle.mininglittlemaid.maid.mode.mining
message.mininglittlemaid.ore_above
message.mininglittlemaid.ore_below
message.mininglittlemaid.ore_unreachable
message.mininglittlemaid.inventory_full
message.mininglittlemaid.no_torch
gui.mininglittlemaid.chat_notify
gui.mininglittlemaid.option.on
gui.mininglittlemaid.option.off
config.mininglittlemaid.section.mining
config.mininglittlemaid.maxVeinSize
config.mininglittlemaid.minLightLevel
config.mininglittlemaid.torchCooldown
config.mininglittlemaid.combatReturnDelay
config.mininglittlemaid.enableDebugLog
```

## 7) Agent workflow checklist

Before editing:
1. Find a nearby analogous implementation and mirror its style.
2. Confirm the right Gradle task(s) (`tasks --all` if uncertain).
3. Determine whether changes are client-only, server-only, or shared.

After editing:
1. Run targeted verification first (filtered `test --tests ...` when applicable).
2. Run `./gradlew.bat check` before handoff.
3. Ensure diff does not contain unrelated formatting churn.
4. **runClient 需要用户确认**：执行 `runClient` 前必须向用户提出确认，不得自行启动。

## 8) Packet-specific checklist

For changes under `network`:
1. Keep packet `TYPE` identifier stable and unique.
2. Maintain `STREAM_CODEC` encode/decode symmetry.
3. Keep side checks explicit (`isServerbound` / `isClientbound`).
4. Follow existing enqueue/handler flow patterns.
5. Keep boundary nullability checks explicit.

## 9) Do not assume

- Auto-format/lint tooling exists (it currently does not).
- Tests exist for every module.
- Cursor/Copilot policy files exist (none found right now).

## 10) Quick commands

- Build: `./gradlew.bat build`
- Check: `./gradlew.bat check`
- Test all: `./gradlew.bat test`
- Test class: `./gradlew.bat test --tests "pkg.ClassName"`
- Test method: `./gradlew.bat test --tests "pkg.ClassName.methodName"`
- Run client: `./gradlew.bat runClient`
- Run data gen: `./gradlew.bat runData`

## 11) Git commit workflow

- 仓库地址：`https://github.com/LonelyGEO/EnhancedLittleMaidAI.git`
- **Agent 主动负责提交**：每次代码改动完成后，Agent 应主动执行 `git add` + `git commit`，不等待用户提醒。提交信息用中文，简洁描述改动目的。
- **SSH / 连接报错先诊断再提问**：遇到 SSH 权限、认证失败、远程连接等问题时，先自行排查（检查 remote、分支状态等），无法解决再向用户提问协助。
- **不可逆操作必须征得用户同意**：以下操作**绝对禁止**不经用户明确同意就执行：
  - `git push --force` / `--force-with-lease`
  - `git reset --hard`
  - `git rebase`（含 `--interactive`）
  - `git branch -D` 删除分支
  - `git commit --amend`（已推送的 commit）
  - 以及其他会修改已推送历史或破坏工作区的操作
- **修改 `.gitignore` 必须征得用户同意**：Agent 不得自行增删 `.gitignore` 条目。如确需修改，先向用户说明理由并取得确认。
- **禁止绕过 `.gitignore`**：不得使用 `git add -f` 等变相手段强制添加被忽略文件。若提交时发现文件被忽略，应告知用户并征求处理方案。
- 每次提交前检查 `git status` 和 `git diff`，确保不包含敏感信息（密钥、token 等）。
- **提交前主动提出版本变更建议**：每次完成代码改动后，Agent 应主动根据 §13 的版本位规则进行判断。PATCH 级别（Bug 修复、小调整）可自行决定并变更版本号；MINOR 及以上（新功能、架构重写）必须向用户确认后变更。

### GitHub Release 发布

- Agent **不得自行发布任何 Release**（包括 Beta 和正式版），必须先向用户提出并取得确认。
- 当前版本号 < 1.0.0 时，发布一律标记为 **Pre-release（Beta）**：
  ```
  gh release create v0.x.x build/libs/*.jar --title "v0.x.x-beta" --prerelease
  ```
- 版本号达 1.0.0 后默认改为正式 Release。

## 12) Versioning

- 当前版本: `0.9.2-neoforge+mc1.21.1`
- 后缀 `-neoforge+mc1.21.1` 为平台标识，保持不变

| 版本位 | 触发条件 |
|--------|---------|
| PATCH (`0.1.x`) | Bug 修复、参数微调、语言文件补充 |
| MINOR (`0.x.0`) | 新增功能（每完成 ROADMAP 中一项） |
| MAJOR (`x.0.0`) | 功能基本完整时升至 `1.0.0`；架构重写或 MC 版本升级 |

规则：
- 版本号变更单独一条 commit，格式 `release: 0.x.y`
- PATCH、MINOR 级别（Bug 修复、参数微调、语言文件补充）Agent 可自行决定并变更，无需等待确认
- MAJOR 版本迭代前**必须向用户确认**，不得自行决定发版
- 发版时在 `WorkingPlan.md` 记录该版本已完成的功能

Keep this file updated when tooling/rules/project conventions change.