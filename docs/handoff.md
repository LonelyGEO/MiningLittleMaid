# 工作交接文档（2026-04-29）

## 分支状态

| 分支 | 状态 | 说明 |
|------|------|------|
| `Origin` (主分支) | 当前工作分支 | `v0.5.1`，含完整采矿 AI + Config |
| `gui-rewrite` | **已废弃（已删除）** | 鼠标回中修复方案已合并到 `Origin` |

## gui-rewrite 分支关键成果

### 1. 鼠标回中问题 — 已根本性修复

**根因**：Mining Config Tab 打开时经过 `Minecraft.setScreen(null)` → `MouseHandler.grabMouse()` → 鼠标强制居中到窗口中心 (427,240)。Attack Config Tab 不居中是因为从已有的 GUI Screen 切换（`old` 不为 null），没有走 null → GUI 的过渡。

**证据**：通过 `ScreenEvent.Opening` 埋点发现：
- Attack: `old=EmptyBackpackContainerScreen`，鼠标保持原位
- Mining: `old=null`，鼠标始终在 (427,240)

**修复文件**：

| 文件 | 改动 |
|------|------|
| `client/MiningLittleMaidClient.java` | 监听 `ScreenEvent.Closing`，每次 Screen 关闭时通过 GLFW 保存鼠标坐标到 `savedMouseX/savedMouseY` |
| `client/gui/MiningTaskConfigGui.java` | `init()` 中调用 `super.init()` 后，若 `hasSaved=true`，通过 `GLFW.glfwSetCursorPos()` 恢复到保存的位置 |

**核心逻辑**：
```
任何 Screen 关闭 → Closing 事件 → 保存鼠标位置
Mining Config Tab 打开 → setScreen(null) → 鼠标居中
→ MiningTaskConfigGui.init() → 恢复到保存位置 ✅
```

### 2. Screen 注册机制确认

- `@EventBusSubscriber(modid = "mininglittlemaid", value = Dist.CLIENT, bus = Bus.MOD)` **正常工作**
- 之前的 `System.out.println` 日志被 Gradle 吞掉导致误判"未加载"
- 使用 `LogManager.getLogger()` 的 `ERROR` 级别日志可正常输出
- 双重注册（`@Mod` 构造函数 + `@EventBusSubscriber`）会导致 Crash：`Duplicate attempt to register screen`

### 3. 合并结果

| 文件 | 操作 | 说明 |
|------|------|------|
| `client/MiningLittleMaidClient.java` | 替换 | 新增 `ScreenEvent.Closing` 监听 + `savedMouseX/Y` |
| `client/MiningLittleMaidScreenSetup.java` | 新增 | Screen 注册（`RegisterMenuScreensEvent`） |
| `client/gui/MiningTaskConfigGui.java` | 修改 | `init()` 中添加鼠标恢复逻辑（保留全部矿石开关逻辑） |
| `MiningLittleMaid.java` | 保持 | 当前版本已正确，无需修改 |
| `TaskMining.java` | 保持 | `getTaskConfigGuiProvider()` 保留 |
| `MiningTaskConfigContainer.java` | 保持 | `TYPE` 模式正确 |

## 待修复项（未实施）

| 问题 | 文件 | 方向 |
|------|------|------|
| 矿石重复通知 | `MaidMineMoveTask.java:44`, `TaskMining.java:38` | `isOrePaused` 无条件 return，`ORE_PAUSE_TICKS` → 200 |
| 火把刷屏 | `MaidMineTorchPlaceTask.java` | 冷却计时器替代亮度重置 |
| 洞顶裸露矿探测 | `MaidMineMoveTask.java` | BFS 无果后垂直列扫描 O(14)（性能优先） |
| 矿石种类 GUI | `MiningTaskConfigGui.java` | 推倒重建，对齐 TLM AttackTab 架构 |

## 关键 API 备忘

| API | 位置 | 用途 |
|-----|------|------|
| `ScreenEvent.Closing` | NeoForge GAME bus | 任何 Screen 关闭时触发 |
| `ScreenEvent.Opening` | NeoForge GAME bus | 任何 Screen 打开时触发 |
| `RegisterMenuScreensEvent` | MOD bus | Screen 工厂注册 |
| `GLFW.glfwGetCursorPos` | LWJGL | 获取物理鼠标位置 |
| `GLFW.glfwSetCursorPos` | LWJGL | 设置物理鼠标位置 |
| `Minecraft.getInstance().screen` | Minecraft | 当前 Screen（可能为 null） |

## 调试技巧

- `LogManager.getLogger("CustomName").error(...)` 在 `runClient` 控制台可见
- `System.out.println` 被 Gradle 吞掉，**不要用于调试**
- `ScreenEvent.Opening` 的 `event.getCurrentScreen()` 返回旧 Screen，`event.getNewScreen()` 返回新 Screen
- `ScreenEvent.Opening` 不触发 null Screen 切换（`setScreen(null)` 不会触发 Opening）
