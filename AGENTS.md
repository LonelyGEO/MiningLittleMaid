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
- **Mod ID**: `mining_little_maid`
- **Base package**: `com.github.lonelygeo.mininglittlemaid`
- **Dependency**: Touhou Little Maid (`libs/touhoulittlemaid-1.5.2-neoforge+mc1.21.1-all.jar`)

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

- `MiningLittleMaid` — `@Mod` 主类，注册音效和网络包
- `MiningAddonPlugin` — `@LittleMaidExtension` 扩展入口，注册 `TaskMining`

### Package roles

| 包 | 职责 |
|---|---|
| `task.*` | 任务定义 + AI 行为（TaskMining, MaidMineMoveTask, MaidMineBreakTask, MiningFavorGate） |
| `init.*` | 注册（InitSounds 音效 DeferredRegister） |
| `network.*` | 网络包（MiningChatNotifyToggleMessage，record 类型） |
| `event.*` | 客户端 GUI 事件（ClientGuiEventHandler） |

### Task system

`TaskMining` implements `IFarmTask` (TouhouLittleMaid 的农场任务接口):

- `canHarvest()` — 检查镐子 + 矿石类型
- `harvest()` — 调用 `maid.destroyBlock(pos)`
- `createBrainTasks()` — 返回两个 AI: `MaidMineMoveTask`(优先级5) + `MaidMineBreakTask`(优先级6)
- `onFunctionCallSwitch()` — 切换任务时自动从背包装备镐子

### AI behaviors

| 类 | 父类 | 职责 |
|---|---|---|
| `MaidMineMoveTask` | `MaidCheckRateTask` | BFS 搜索矿石（嗅探半径由好感度决定），找不到时随机跟随主人 |
| `MaidMineBreakTask` | `Behavior<EntityMaid>` | 到达 → 检查高度差 → 挖掘 → 近邻搜索下一个矿石 |

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
task.mining_little_maid.mining
task.mining_little_maid.mining.desc
task.mining_little_maid.mining.condition.has_pickaxe
subtitle.mining_little_maid.maid.mode.mining
message.mining_little_maid.ore_above_below
gui.mining_little_maid.chat_notify
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

- 仓库地址：`https://github.com/LonelyGEO/MiningLittleMaid.git`
- **Agent 主动负责提交**：每次代码改动完成后，Agent 应主动执行 `git add` + `git commit`，不等待用户提醒。提交信息用中文，简洁描述改动目的。
- **SSH / 连接报错先诊断再提问**：遇到 SSH 权限、认证失败、远程连接等问题时，先自行排查（检查 remote、分支状态等），无法解决再向用户提问协助。
- **不可逆操作必须征得用户同意**：以下操作**绝对禁止**不经用户明确同意就执行：
  - `git push --force` / `--force-with-lease`
  - `git reset --hard`
  - `git rebase`（含 `--interactive`）
  - `git branch -D` 删除分支
  - `git commit --amend`（已推送的 commit）
  - 以及其他会修改已推送历史或破坏工作区的操作
- 每次提交前检查 `git status` 和 `git diff`，确保不包含敏感信息（密钥、token 等）。

Keep this file updated when tooling/rules/project conventions change.
