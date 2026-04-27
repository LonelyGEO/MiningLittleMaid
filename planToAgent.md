# Plan to Agent

## Planned features

- [ ] Add configurable ore whitelist via block tag

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
- `canMineAtLevel()` 无需修改——它已经委托给 `isMineableOre()`

- [ ] Support vein mining (breaking connected ore blocks in one go)

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
        continue
    maid.destroyBlock(current)     // 自带音效+粒子+掉落
    count++
    对 current 的 6 个相邻方向:
        neighbor ← current.offset(dir)
        if neighbor 未访问 且 isMineableOre(neighbor) 且 isSameOreType(first, neighbor):
            visited.add(neighbor)
            queue.add(neighbor)
```

---

#### 可配置项

`maxVeinSize`：可在 `ModConfigSpec` 或常量中定义，范围 2~64，默认 8。

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
- [ ] Add tool durability check before breaking
- [ ] Add "stop when inventory full" logic
- [ ] Add torch placement while mining (light up dark areas)
- [ ] Support the "Create" mod's drill tool as a pickaxe alternative
- [ ] Add custom ambient sound for mining (instead of reusing MAID_FARM sound)
