# Plan to Agent

## Planned features

- [x] Add configurable ore whitelist via block tag

### 1) Block Tag 矿石白名单（可配置）

**目标**：将 `MiningFavorGate.isMineableOre()` 从硬编码 10 种矿石改为由一个自定义 block tag 驱动，用户/整合包/其他模组可通过 datapack 增删可挖掘矿石，无需修改本 mod 代码。

---

#### 接口设计

**自定义 Tag**：
- Tag 路径：`mining_little_maid:mineable_ores`
- 文件位置：`src/main/resources/data/mining_little_maid/tags/block/mineable_ores.json`
- 默认值：`replace: false`（合并模式），包含所有原版矿石

```json
{
    "replace": false,
    "values": [
        "#minecraft:coal_ores",
        "#minecraft:copper_ores",
        "#minecraft:iron_ores",
        "#minecraft:gold_ores",
        "#minecraft:lapis_ores",
        "#minecraft:redstone_ores",
        "#minecraft:diamond_ores",
        "#minecraft:emerald_ores",
        "minecraft:nether_quartz_ore",
        "minecraft:ancient_debris"
    ]
}
```

> 注：8 种已有 vanilla `#minecraft:*_ores` tag 的直接引用 tag（自动覆盖对应的普通和深层变体），下界石英和远古残骸没有现成的 vanilla tag，所以用单个方块 ID。

**Java 侧变更（`MiningFavorGate.java`）**：

| 变更点 | 旧代码 | 新代码 |
|--------|--------|--------|
| Tag 引用 | 逐条 `state.is(BlockTags.COAL_ORES) \|\| ...` | 单一 `state.is(MINEABLE_ORES)` |
| 常量定义 | 无 | `public static final TagKey<Block> MINEABLE_ORES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MiningLittleMaid.MOD_ID, "mineable_ores"));` |
| `isMineableOre(BlockState)` | 10 次 `||` 判断 | 1 行 `return state.is(MINEABLE_ORES);` |

完整修改后的 `MiningFavorGate.isMineableOre()`：
```java
public static boolean isMineableOre(BlockState state) {
    return state.is(MINEABLE_ORES);
}
```

**Tag 引用方式**：在 `MiningFavorGate` 中声明 `TagKey<Block>` 常量，而非运行时拼接字符串，确保类型安全和编译时检查。

---

#### 实现步骤

1. 新建 `src/main/resources/data/mining_little_maid/tags/block/mineable_ores.json`
2. 修改 `MiningFavorGate.java`：
   - 新增 import：`TagKey`、`Registries`、`MiningLittleMaid`
   - 新增静态常量 `MINEABLE_ORES`
   - 重构 `isMineableOre()` 为单行 tag 检测
   - 移除不再需要的 `BlockTags` 和 `Blocks` 的 import（或检查是否在其他方法中使用）

---

#### 涉及文件

| 文件 | 操作 |
|------|------|
| `.../data/.../tags/block/mineable_ores.json` | 新建 |
| `MiningFavorGate.java` | 修改约 15 行 |

---

#### 兼容性 / 扩展性

- **下界石英** 和 **远古残骸** 没有 vanilla `#minecraft:*_ores` tag，须用方块 ID 字面量
- `replace: false` 确保其他模组自动注册自己的矿石时零配置生效
- `canMineAtLevel()` 已退化为仅调 `isMineableOre()`→ 删除此方法，调用处直接改 `isMineableOre()`

- [x] Support vein mining (breaking connected ore blocks in one go)

### 2) 矿脉连锁挖掘（Vein Mining）

**目标**：好感度等级 3 解锁。站在同一位置，BFS 连锁挖掉相连的同种矿石，每块扣耐久，完成后播汇总粒子。

---

#### 用户选择

| 参数 | 选择 |
|------|------|
| 连锁上限 | 可配置 `maxVeinSize`（默认 8） |
| 连锁条件 | 同种矿石（含深层变体，如 `iron_ore` ↔ `deepslate_iron_ore`） |
| 好感度门控 | 仅等级 3（好感度 ≥ 384）可用 |
| 耐久结算 | 每块扣 1 点（连锁 N 块扣 N 点） |
| 视觉反馈 | 连锁完成时播汇总粒子 |

---

#### 好感度行为总结

| 等级 | 好感度 | 嗅探半径 | 连锁挖掘 | 可挖矿石 |
|------|--------|---------|---------|---------|
| 0 | 0-63 | 1 | ✗ | 全部 |
| 1 | 64-191 | 2 | ✗ | 全部 |
| 2 | 192-383 | 2 | ✗ | 全部 |
| 3 | 384+ | 3 | ✓（maxVeinSize 限制） | 全部 |

---

#### 接口设计

**`MiningFavorGate.java` 新增**：

```java
// 好感度等级 3 才可连锁
public static boolean canVeinMine(int favorLevel) {
    return favorLevel >= 3;
}

// 同种矿石判定（iron_ore ↔ deepslate_iron_ore 算同种）
public static boolean isSameOreType(BlockState a, BlockState b) {
    if (a.getBlock() == b.getBlock()) return true;
    return getBaseOreName(a).equals(getBaseOreName(b));
}

private static String getBaseOreName(BlockState state) {
    return BuiltInRegistries.BLOCK.getKey(state.getBlock())
            .getPath().replace("deepslate_", "");
}
```

**`MaidMineBreakTask.java` 修改**：

在 `start()` 方法中，将原单块 `task.harvest()` 替换为条件分支：

```java
int count;
if (MiningFavorGate.canVeinMine(maid.getFavorabilityManager().getLevel())) {
    count = veinMineBFS(worldIn, maid, targetPos);
} else {
    task.harvest(maid, targetPos, worldIn.getBlockState(targetPos));
    count = 1;
}
// 耐久结算
ItemStack pickaxe = maid.getMainHandItem();
if (count > 0 && pickaxe.getItem() instanceof PickaxeItem) {
    pickaxe.hurtAndBreak(count, maid, EquipmentSlot.MAINHAND);
}
// 汇总粒子
if (count > 1) {
    worldIn.sendParticles(ParticleTypes.HAPPY_VILLAGER,
        maid.getX(), maid.getY() + 1.5, maid.getZ(),
        count * 2, 0.5, 0.5, 0.5, 0.1);
}
```

**BFS 连锁算法**：

```
输入：首块矿石 pos，世界 world，女仆 maid
输出：挖掉的总块数 count

Queue<BlockPos> queue ← [pos]
Set<BlockPos> visited ← [pos]
count ← 0

while queue 非空 且 count < maxVeinSize:
    current ← queue.poll()
    if !isMineableOre(state) 或 !maid.canDestroyBlock(current):
        continue   // 跳过不计入 count
    if !maid.destroyBlock(current):  // 返回 false 也跳过
        continue
    count++
    对 current 的 6 个相邻方向:
        neighbor ← current.offset(dir)
        if neighbor 未访问 且 isMineableOre(neighbor) 且 isSameOreType(first, neighbor):
            visited.add(neighbor)
            queue.add(neighbor)
```

---

#### 可配置项

`maxVeinSize`：通过 `Config.MAX_VEIN_SIZE.get()` 读取，范围 2~64，默认 8。

---

#### 涉及文件

| 文件 | 操作 |
|------|------|
| `MiningFavorGate.java` | 新增 `canVeinMine()`、`isSameOreType()`、`getBaseOreName()` |
| `MaidMineBreakTask.java` | 新增 BFS 连锁算法、耐久结算、汇总粒子；修改 `start()` 入口分支 |

---

#### 向下兼容

- 好感度 < 3 时行为与当前完全一致（单块挖掘）
- 连锁完毕后仍调用 `findAdjacentOre()` 寻找下一个矿脉

- [x] Add tool durability check before breaking

### 3) 工具耐久度预检 + 自动换镐

**目标**：好感度等级 1 解锁。镐子耐久不足时自动从背包换备用镐，无备用镐则取消采矿任务变回空闲。以独立 Brain Task 实现，资源占用低。

---

#### 用户选择

| 参数 | 选择 |
|------|------|
| 预检逻辑 | 耐久不足 → 自动换备用镐 → 仍无 → 取消任务 |
| 耐久阈值 | 剩余耐久 < `maxVeinSize`(8) 时触发 |
| 好感度门控 | 仅等级 1+（好感度 ≥ 64）可用 |
| 实现方式 | 独立 Brain Task，优先级 4（高于 Move/5、Break/6） |

---

#### 好感度行为总结

| 等级 | 好感度 | 嗅探半径 | 耐久预检 | 自动换镐 | 连锁挖掘 |
|------|--------|---------|---------|---------|---------|
| 0 | 0-63 | 1 | ✗ | ✗ | ✗ |
| 1 | 64-191 | 2 | ✓ | ✓ | ✗ |
| 2 | 192-383 | 2 | ✓ | ✓ | ✗ |
| 3 | 384+ | 3 | ✓ | ✓ | ✓ |

---

#### 接口设计

**`MiningFavorGate.java` 新增**：

```java
public static boolean canCheckDurability(int favorLevel) {
    return favorLevel >= 1;
}
```

**新增 `MaidMineDurabilityCheckTask.java`**（优先级 4）：

```java
public class MaidMineDurabilityCheckTask extends MaidCheckRateTask {
    private static final int CHECK_RATE = 60; // 每 3 秒一次

    public MaidMineDurabilityCheckTask() {
        super(ImmutableMap.of()); // 无需特定 memory 条件
        this.setMaxCheckRate(CHECK_RATE);
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTime) {
        // 好感度 < 1 不检查
        if (!MiningFavorGate.canCheckDurability(maid.getFavorabilityManager().getLevel())) {
            return;
        }
        // 当前镐子耐久足够，无需处理
        if (hasDurablePickaxe(maid)) {
            return;
        }
        int minDurability = Config.MAX_VEIN_SIZE.get();
        // 尝试从背包换一把耐久 ≥ minDurability 的镐子
        if (TaskEquipUtil.tryEquipFromBackpack(maid, stack ->
                MiningFavorGate.isMiningTool(stack)
                && (stack.getMaxDamage() - stack.getDamageValue()) >= minDurability)) {
            return;
        }
        // 无备用镐 → 取消采矿任务，变回空闲
        maid.setTask(null);
    }

    private boolean hasDurablePickaxe(EntityMaid maid) {
        ItemStack mainHand = maid.getMainHandItem();
        if (MiningFavorGate.isMiningTool(mainHand)) {
            return (mainHand.getMaxDamage() - mainHand.getDamageValue()) >= Config.MAX_VEIN_SIZE.get();
        }
        return false;
    }
}
```

**`TaskMining.createBrainTasks()` 修改**：

```java
@Override
public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
    MaidMineDurabilityCheckTask checkTask = new MaidMineDurabilityCheckTask();
    MaidMineMoveTask moveTask = new MaidMineMoveTask(this, 0.6f, VERTICAL_SEARCH_RANGE);
    MaidMineBreakTask breakTask = new MaidMineBreakTask(this);
    return Lists.newArrayList(
        Pair.of(4, checkTask),   // 新增：最高优先，耐久检查
        Pair.of(5, moveTask),
        Pair.of(6, breakTask)
    );
}
```

---

#### 为什么是独立 Brain Task

| 对比项 | `canHarvest()` 链 | 独立 Brain Task |
|--------|------------------|----------------|
| 能否取消任务 | ❌ 不能（谓词无副作用） | ✅ 直接操作任务状态 |
| 触发频率 | 高（BFS 每节点一次） | 低（60 ticks 一次） |
| 资源开销 | 高 | 极低 |
| 架构 | 勉强 | 符合 `MaidCheckRateTask` 模式 |

---

#### 涉及文件

| 文件 | 操作 |
|------|------|
| `MiningFavorGate.java` | 新增 `canCheckDurability()` |
| `MaidMineDurabilityCheckTask.java` | 新建 |
| `TaskMining.java` | `createBrainTasks()` 加入优先级 4 的新任务 |

---

#### 向下兼容

- 好感度 0（新女仆）不检查耐久，行为与当前完全一致
- 不影响现有 Move/Break 任务的优先级和执行逻辑

- [x] Add "stop when inventory full" logic

### 4) 背包满时停止 + 通知

**目标**：所有好感度等级生效。女仆背包满时自动停止采矿、通知玩家、转为空闲。以独立 Brain Task 实现，与 Feature 3 同级（优先级 4）。

---

#### 用户选择

| 参数 | 选择 |
|------|------|
| 满包行为 | 停止挖矿 → 通知玩家 → 转为空闲 |
| 判定标准 | 所有可用栏位全满（含背包模组栏位） |
| 好感度门控 | 不挂钩，所有等级生效 |
| 实现方式 | 独立 Brain Task，优先级 4 |

---

#### 主 mod API 分析

| API | 返回类型 | 用途 |
|-----|---------|------|
| `maid.getAvailableInv(true)` | `CombinedInvWrapper` | 所有栏位（主物品栏 + 背包模组栏位） |
| `inv.getSlots()` | `int` | 总栏位数 |
| `inv.getStackInSlot(i)` | `ItemStack` | 访问第 i 格 |

---

#### 接口设计

**新增 `MaidMineInventoryCheckTask.java`**（优先级 4，与耐久检查同级）：

```java
public class MaidMineInventoryCheckTask extends MaidCheckRateTask {
    private static final int CHECK_RATE = 60; // 每 3 秒一次
    private static final String FULL_NOTIFY_KEY = "message.mining_little_maid.inventory_full";

    public MaidMineInventoryCheckTask() {
        super(ImmutableMap.of()); // 无需特定 memory 条件
        this.setMaxCheckRate(CHECK_RATE);
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTime) {
        if (isInventoryFull(maid)) {
            // 通知主人
            if (maid.getOwner() instanceof ServerPlayer player) {
                player.sendSystemMessage(
                    Component.translatable(FULL_NOTIFY_KEY));
            }
            // 取消采矿任务，变回空闲
            maid.setTask(null);
        }
    }

    private static boolean isInventoryFull(EntityMaid maid) {
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) return false;
            if (stack.getCount() < stack.getMaxStackSize()) return false;
        }
        return true;
    }
}
```

**`TaskMining.createBrainTasks()` 修改**：

```java
@Override
public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
    MaidMineDurabilityCheckTask durabilityTask = new MaidMineDurabilityCheckTask();
    MaidMineInventoryCheckTask inventoryTask = new MaidMineInventoryCheckTask();
    MaidMineMoveTask moveTask = new MaidMineMoveTask(this, 0.6f, VERTICAL_SEARCH_RANGE);
    MaidMineBreakTask breakTask = new MaidMineBreakTask(this);
    return Lists.newArrayList(
        Pair.of(4, durabilityTask),   // 耐久检查
        Pair.of(4, inventoryTask),    // 背包满检查（同级，两项同时执行）
        Pair.of(5, moveTask),
        Pair.of(6, breakTask)
    );
}
```

---

#### 新增翻译键

| 键 | zh_cn | en_us |
|---|-------|-------|
| `message.mining_little_maid.inventory_full` | `女仆的背包已满，停止采矿` | `Maid's inventory is full, mining stopped` |

---

#### 涉及文件

| 文件 | 操作 |
|------|------|
| `MaidMineInventoryCheckTask.java` | 新建 |
| `TaskMining.java` | `createBrainTasks()` 加入优先级 4 的新任务 |
| `zh_cn.json` | 新增翻译键 |
| `en_us.json` | 新增翻译键 |

---

#### 向下兼容

- `getAvailableInv(true)` 即使未安装背包模组也返回基础物品栏，行为无变化
- 独立 Brain Task 不影响现有逻辑

- [x] Add torch placement while mining (light up dark areas)

### 5) 移动时自动放置火把

**目标**：所有好感度等级生效。女仆在采矿任务中定期检测脚边亮度，低于可配置阈值时自动放置火把。以独立 Brain Task 实现（优先级 4），面向多模组服务器深度优化性能。

---

#### 用户选择

| 参数 | 选择 |
|------|------|
| 放置时机 | 移动经过时，AI 定期检测周围亮度 |
| 放置位置 | 脚边（向下搜索最近可放置面） |
| 火把来源 | 背包消耗；无火把时通知玩家 |
| 好感度门控 | 不挂钩 |
| 实现方式 | 独立 Brain Task，优先级 4 |

---

#### 性能优化设计（多模组服务器优先）

| 优化点 | 说明 |
|--------|------|
| 检测周期 | 60 ticks（3 秒），继承 `MaidCheckRateTask` 框架 |
| 冷却时间 | 120 ticks（6 秒），可配置 |
| 亮度查询 | `world.getMaxLocalRawBrightness(pos)` — O(1) |
| 静止跳过 | 坐标与上次相同时跳过（避免同一暗处反复检查） |
| 最昂贵操作 | `world.setBlock` 仅在所有条件满足时执行一次 |

---

#### 接口设计

**`MiningLittleMaid.java` 注册配置**：

```java
public MiningLittleMaid(IEventBus modEventBus, ModContainer modContainer) {
    InitSounds.SOUNDS.register(modEventBus);
    modEventBus.addListener(this::registerPayloadHandlers);
    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
}
```

**新增 `config/Config.java`**（共享配置类，供 Feature 2/3/5 使用）：

```java
package com.github.lonelygeo.mininglittlemaid.config;

public class Config {
    public static final ModConfigSpec SPEC;

    // Feature 2: 矿脉连锁
    public static final ModConfigSpec.IntValue MAX_VEIN_SIZE;

    // Feature 5: 火把
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
```

**运行时生成文件**：`config/mining_little_maid-common.toml`

```toml
[Mining Little Maid 配置]
# 矿脉连锁最大块数（仅好感度等级 3 生效）
# 范围: 2 ~ 64
maxVeinSize = 8
# 亮度低于此值时女仆放置火把
# 范围: 0 ~ 15
minLightLevel = 7
# 火把放置冷却时间（tick，20 tick = 1 秒）
# 范围: 20 ~ 600
torchCooldownTicks = 120
```

**新增 `MaidMineTorchPlaceTask.java`**（优先级 4）：

```java
public class MaidMineTorchPlaceTask extends MaidCheckRateTask {
    private static final int CHECK_RATE = 60; // 每 3 秒
    private static final String NO_TORCH_KEY = "message.mining_little_maid.no_torch";
    private BlockPos lastPos = BlockPos.ZERO;
    private long lastPlaceTime; // 冷却计时

    public MaidMineTorchPlaceTask() {
        super(ImmutableMap.of());
        this.setMaxCheckRate(CHECK_RATE);
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTime) {
        // 坐标未变 → 跳过（避免同一位置反复检查）
        if (maid.blockPosition().equals(lastPos)) return;
        lastPos = maid.blockPosition().immutable();

        // 亮度足够 → 跳过
        if (world.getMaxLocalRawBrightness(maid.blockPosition()) >= Config.MIN_LIGHT_LEVEL.get()) return;

        // 冷却中 → 跳过
        long cooldown = Config.TORCH_COOLDOWN_TICKS.get();
        if (gameTime - lastPlaceTime < cooldown) return;

        // 找脚边可放置面
        BlockPos placePos = findPlaceableSurface(world, maid.blockPosition());
        if (placePos == null) return;

        // 消耗火把
        if (!consumeTorch(maid)) {
            if (maid.getOwner() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable(NO_TORCH_KEY));
            }
            return;
        }

        // 放置火把
        world.setBlock(placePos, Blocks.TORCH.defaultBlockState(), 3);
        lastPlaceTime = gameTime;
    }

    private BlockPos findPlaceableSurface(ServerLevel world, BlockPos maidPos) {
        BlockPos.MutableBlockPos check = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy >= -3; dy--) {
            check.set(maidPos.getX(), maidPos.getY() + dy, maidPos.getZ());
            if (world.getBlockState(check).isFaceSturdy(world, check, Direction.UP)
                    && world.getBlockState(check.above()).isAir()) {
                return check.above().immutable();
            }
        }
        return null;
    }

    private boolean consumeTorch(EntityMaid maid) {
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (inv.getStackInSlot(i).is(Items.TORCH)) {
                inv.extractItem(i, 1, false);
                return true;
            }
        }
        return false;
    }
}
```

**`TaskMining.createBrainTasks()` 修改**：

```java
@Override
public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
    return Lists.newArrayList(
        Pair.of(4, new MaidMineDurabilityCheckTask()),
        Pair.of(4, new MaidMineInventoryCheckTask()),
        Pair.of(4, new MaidMineTorchPlaceTask()),
        Pair.of(5, new MaidMineMoveTask(this, 0.6f, VERTICAL_SEARCH_RANGE)),
        Pair.of(6, new MaidMineBreakTask(this))
    );
}
```

---

#### 新增翻译键

| 键 | zh_cn | en_us |
|---|-------|-------|
| `message.mining_little_maid.no_torch` | `女仆需要火把，但背包里没有` | `Maid needs a torch, but none in inventory` |

---

#### AI 优先级总览（Feature 3/4/5 完成后）

| 优先级 | 任务 | 功能 | 检查周期 |
|--------|------|------|---------|
| 4 | `DurabilityCheckTask` | 耐久预检 + 自动换镐 | 60 ticks |
| 4 | `InventoryCheckTask` | 背包满时停止 | 60 ticks |
| 4 | `TorchPlaceTask` | 自动放火把 | 60 ticks + 120 ticks 冷却 |
| 5 | `MineMoveTask` | BFS 搜索矿石 | 120 ticks（未找到时） |
| 6 | `MineBreakTask` | 挖掘矿石 | 20 ticks |

---

#### 涉及文件

| 文件 | 操作 |
|------|------|
| `config/Config.java` | 新建 |
| `MiningLittleMaid.java` | `registerConfig` 一行 |
| `MaidMineTorchPlaceTask.java` | 新建 |
| `TaskMining.java` | `createBrainTasks()` 加入优先级 4 |
| `zh_cn.json` | `message.mining_little_maid.no_torch` |
| `en_us.json` | `message.mining_little_maid.no_torch` |

- [x] Support mining tools from other mods via Item Tag

### 6) 模组采矿工具兼容（Item Tag 驱动）

**目标**：将"可采矿工具"判定从硬编码 `instanceof PickaxeItem` 改为 Item Tag `#mining_little_maid:mining_tools`，使 Create 钻头等第三方工具自动被识别，无需本 mod 修改代码。

> 注：Create 本体没有手持钻头（其 `Mechanical Drill` 是方块实体），但 Create: Crafts & Additions 等附属模组提供手持钻头。Item Tag 方案对此类工具通用。

---

#### 用户选择

| 参数 | 选择 |
|------|------|
| 识别方式 | Item Tag 白名单 + 已知类 fallback（fallback 留空待扩展） |
| 耐久检查 | 通用 API（`getMaxDamage() - getDamageValue()`），不针对特定模组 |
| 自动装备优先级 | 耐久优先（选剩余耐久最高的工具） |

---

#### Item Tag

**文件**：`src/main/resources/data/mining_little_maid/tags/item/mining_tools.json`

```json
{
    "replace": false,
    "values": [
        "#minecraft:pickaxes"
    ]
}
```

`#minecraft:pickaxes` 覆盖所有材质（木/石/铁/金/钻石/下界合金）。其他模组追加自己的工具即可：

```json
// 某个模组的 data/<modid>/tags/item/mining_little_maid/mining_tools.json
{
    "values": [
        "createaddition:drill",
        "othermod:mining_laser"
    ]
}
```

---

#### 接口设计

**`MiningFavorGate.java` 新增 `isMiningTool()` 和 `MINING_TOOLS` 常量**：

```java
public static boolean isMiningTool(ItemStack stack) {
    if (stack.is(MINING_TOOLS)) return true;
    // fallback：已知模组工具类（按需扩展）
    return false;
}
```

**`ItemTags` 常量引用**：

```java
public static final TagKey<Item> MINING_TOOLS =
    TagKey.create(Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(MiningLittleMaid.MOD_ID, "mining_tools"));
```

---

#### 涉及修改点（全局替换 `instanceof PickaxeItem`）

| 文件 | 方法 | 变更 |
|------|------|------|
| `TaskMining.java` | `hasPickaxe()` | `instanceof PickaxeItem` → `isMiningTool(stack)` |
| `TaskMining.java` | `onFunctionCallSwitch()` | 同上 |
| `MaidMineDurabilityCheckTask.java` | `hasDurablePickaxe()` | 同上 |
| `MaidMineBreakTask.java` | 耐久结算 | 同上 |

**耐久优先换镐（Feature 3 修改）**：

```java
TaskEquipUtil.tryEquipFromBackpack(maid, stack ->
    MiningToolUtil.isMiningTool(stack)
    && (stack.getMaxDamage() - stack.getDamageValue()) >= maxVeinSize);
```

---

#### 涉及文件

| 文件 | 操作 |
|------|------|
| `data/.../tags/item/mining_tools.json` | 新建 |
| `MiningFavorGate.java` | 新增 `isMiningTool()` + `MINING_TOOLS` 常量 |
| `TaskMining.java` | `instanceof PickaxeItem` → `isMiningTool()` |
| `MaidMineDurabilityCheckTask.java` | 同上 |
| `MaidMineBreakTask.java` | 同上 |

---

#### 向下兼容

- `#minecraft:pickaxes` 包含所有原版镐子，行为完全不变
- 仅替换判断条件，不影响任何运行时逻辑

---

- [ ] Combat detection: auto-switch to combat when monster nearby, return to mining after

### 7) 采矿遇怪自动切换战斗

**目标**：好感度等级 1+ 解锁。采矿时检测到怪物 → 自动装备武器 → 切换到主 mod 的 `TaskAttack` 战斗任务；战斗结束后延迟切回采矿。

---

#### 用户选择

| 参数 | 选择 |
|------|------|
| 检测方式 | 被动 `maid.getTarget()`（信任主 mod 战斗 AI） |
| 切换目标 | 主 mod `TaskAttack`（UID: `touhou_little_maid:attack`） |
| 武器判定 | Item Tag `#mining_little_maid:weapons` |
| 战斗结束延迟 | 可配置，默认 100 ticks（5 秒） |
| 好感度门控 | 等级 1+（好感度 ≥ 64） |
| 无武器 | 静默跳过，保持采矿 |

---

#### 主 mod API 验证

| API | 值 | 用途 |
|-----|-----|------|
| `maid.getTarget()` | `LivingEntity` | 当前攻击目标（主 mod 战斗 AI 自动设置） |
| `TaskManager.findTask(ResourceLocation)` | `Optional<IMaidTask>` | 查找战斗任务 |
| `maid.setTask(IMaidTask)` | — | 切换女仆任务 |
| `MaidTickEvent` | 每 tick 事件 | 战斗结束切回监控（无额外开销） |
| `maid.getPersistentData()` | `CompoundTag` | 存储采矿任务引用 |

---

#### 双组件设计

| 组件 | 类型 | 职责 | 触发频率 |
|------|------|------|---------|
| `MaidMineCombatCheckTask` | Brain Task（优先级 4） | 检测怪物 → 切到战斗 | 60 ticks |
| `MaidMineCombatEventHandler` | Event Subscriber | 战斗结束 → 切回采矿 | MaidTickEvent |

**流程**：

```
[采矿中] → 每3秒 Brain Task 检查
  → 好感度 ≥ 1?                          ✗ → 跳过
  → maid.getTarget() instanceof Monster?  ✗ → 跳过
  → 背包有武器（#mining_little_maid:weapons）?
      ✗ → 跳过（继续挖矿）
  → 自动装备武器到主手
  → persistentData["mining_resume"] = currentTask
  → TaskManager.findTask("touhou_little_maid:attack")
  → maid.setTask(attackTask)

[战斗中] → MaidTickEvent 每 tick
  → persistentData 有 "mining_resume"?
      → getTarget() == null?
          → idleTicks++ >= config.delay?
              → 切回采矿任务 + 清除 flag
      → else
          → idleTicks = 0
```

---

#### 接口设计

**新增 Item Tag**：`data/mining_little_maid/tags/item/weapons.json`

```json
{
    "replace": false,
    "values": [
        "#minecraft:swords",
        "#minecraft:axes"
    ]
}
```

> 其他模组追加自己的武器即可（刀、枪、棒等）。

**Config.java 新增**：

```java
public static final ModConfigSpec.IntValue COMBAT_RETURN_DELAY_TICKS;

// static block 中：
COMBAT_RETURN_DELAY_TICKS = builder
        .comment("战斗结束后等多久切回采矿（tick，20 tick = 1 秒）")
        .defineInRange("combatReturnDelayTicks", 100, 20, 600);
```

**新增 `MaidMineCombatCheckTask.java`**（优先级 4）：

```java
public class MaidMineCombatCheckTask extends MaidCheckRateTask {
    private static final int CHECK_RATE = 60; // 每 3 秒
    private static final String ATTACK_TASK_ID = "touhou_little_maid:attack";
    private static final String RESUME_KEY = "mining_resume";

    public MaidMineCombatCheckTask() {
        super(ImmutableMap.of());
        this.setMaxCheckRate(CHECK_RATE);
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long gameTime) {
        if (maid.getFavorabilityManager().getLevel() < 1) return;

        LivingEntity target = maid.getTarget();
        if (!(target instanceof Monster)) return;

        // 已在战斗任务中 → 跳过
        IMaidTask currentTask = maid.getTask();
        if (currentTask != null
                && "touhou_little_maid:attack".equals(currentTask.getUid().toString())) return;

        // 搜索武器并装备
        if (!tryEquipWeapon(maid)) return; // 无武器，静默跳过

        // 保存当前任务引用用于切回
        if (currentTask != null) {
            maid.getPersistentData().putString(RESUME_KEY,
                    currentTask.getUid().toString());
        }

        // 切换到战斗任务
        TaskManager.findTask(ResourceLocation.parse(ATTACK_TASK_ID))
                .ifPresent(maid::setTask);
    }

    private boolean tryEquipWeapon(EntityMaid maid) {
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (MiningFavorGate.isWeapon(stack)) {
                // 装备到主手
                ItemStack mainHand = maid.getMainHandItem();
                // 先把当前主手物品放回背包
                inv.insertItem(i, mainHand, false);
                // 再装备武器
                inv.extractItem(i, 1, false);
                maid.setItemSlot(EquipmentSlot.MAINHAND, stack.copy());
                return true;
            }
        }
        return false;
    }
}
```

**`MiningFavorGate.java` 新增武器判定**：

```java
public static final TagKey<Item> WEAPONS = TagKey.create(Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(MiningLittleMaid.MOD_ID, "weapons"));

public static boolean isWeapon(ItemStack stack) {
    return stack.is(WEAPONS);
}
```

**新增 `MaidMineCombatEventHandler.java`**（Event Subscriber）：

```java
@EventBusSubscriber(modid = MiningLittleMaid.MOD_ID)
public class MaidMineCombatEventHandler {
    private static final String RESUME_KEY = "mining_resume";
    private static final Map<UUID, Integer> idleCounter = new HashMap<>();

    @SubscribeEvent
    public static void onMaidTick(MaidTickEvent event) {
        EntityMaid maid = event.getMaid();
        UUID id = maid.getUUID();
        String taskId = maid.getPersistentData().getString(RESUME_KEY);
        if (taskId.isEmpty()) {
            idleCounter.remove(id);
            return;
        }

        if (maid.getTarget() != null) {
            idleCounter.remove(id); // 战斗中，重置
            return;
        }

        int ticks = idleCounter.getOrDefault(id, 0) + 1;
        if (ticks >= Config.COMBAT_RETURN_DELAY_TICKS.get()) {
            TaskManager.findTask(ResourceLocation.parse(taskId))
                    .ifPresent(maid::setTask);
            maid.getPersistentData().remove(RESUME_KEY);
            idleCounter.remove(id);
        } else {
            idleCounter.put(id, ticks);
        }
    }
}
```

> 注：`idleCounter` 用静态 `HashMap<UUID, Integer>` 存储而非 NBT，避免频繁 NBT 读写开销。

**`TaskMining.createBrainTasks()` 更新**：

```java
return Lists.newArrayList(
    Pair.of(4, new MaidMineDurabilityCheckTask()),
    Pair.of(4, new MaidMineInventoryCheckTask()),
    Pair.of(4, new MaidMineTorchPlaceTask()),
    Pair.of(4, new MaidMineCombatCheckTask()),   // 新增
    Pair.of(5, new MaidMineMoveTask(this, 0.6f, VERTICAL_SEARCH_RANGE)),
    Pair.of(6, new MaidMineBreakTask(this))
);
```

---

#### AI 优先级总览（全部完成后）

| 优先级 | 任务 | 功能 | 检查周期 |
|--------|------|------|---------|
| 4 | `DurabilityCheckTask` | 耐久预检 + 自动换镐 | 60 ticks |
| 4 | `InventoryCheckTask` | 背包满时停止 | 60 ticks |
| 4 | `TorchPlaceTask` | 自动放火把 | 60 ticks + 120 ticks 冷却 |
| 4 | `CombatCheckTask` | 遇怪切战斗 | 60 ticks |
| 5 | `MineMoveTask` | BFS 搜索矿石 | 120 ticks |
| 6 | `MineBreakTask` | 挖掘矿石 | 20 ticks |

---

#### 涉及文件

| 文件 | 操作 |
|------|------|
| `data/.../tags/item/weapons.json` | 新建 |
| `MiningFavorGate.java` | 新增 `isWeapon()` + `WEAPONS` 常量 |
| `config/Config.java` | 新增 `COMBAT_RETURN_DELAY_TICKS` |
| `MaidMineCombatCheckTask.java` | 新建 |
| `MaidMineCombatEventHandler.java` | 新建 |
| `TaskMining.java` | `createBrainTasks()` 加入优先级 4 |

---

#### 向下兼容

- 好感度 0 时不检测，行为不变
- `MaidTickEvent` 是主 mod 已有事件，订阅无额外开销
- 无武器时静默跳过，不影响采矿正常进行
