# MiningLittleMaid Config 系统技术文档 (v1.0.0)

## 适用模组

Mining Little Maid — Touhou Little Maid 附属模组，NeoForge 1.21.1

---

## 1. 配置入口

```
Config.java (ModConfigSpec Builder)
    ↓
@Mod 构造: modContainer.registerConfig(Type.COMMON, SPEC)
    ↓
运行时 → config/mininglittlemaid-common.toml
    ↓
MiningClothConfig.java (@Dist.CLIENT, AddClothConfigEvent)
    ↓
游戏内: Mods → Touhou Little Maid → 配置 → 采矿
```

---

## 2. Section 结构（5 个 Section，39 项）

```toml
[mining]                         # 通用 (3项)
maxVeinSize = 9                  # 1–64
minLightLevel = 7                # 0–15
enableDebugLog = false

[mining.cooldown]                # 冷却 (6项)
torchCooldownTicks = 120         # 20–600
torchNotifyCooldownTicks = 12000 # 200–72000
combatReturnDelayTicks = 100     # 20–600
combatCooldownTicks = 200        # 60–600
orePauseTicks = 200              # 100–600
oreAlertCooldownTicks = 12000    # 2400–72000

[mining.rate]                    # 检测频率 (6项)
combatCheckRate = 60             # 20–200
durabilityCheckRate = 60         # 20–200
inventoryCheckRate = 60          # 20–200
torchCheckRate = 60              # 20–200
bfsMaxDelay = 120               # 40–600
breakCheckRate = 20              # 10–100

[mining.range]                   # 范围控制 (16项)
bfsSearchRadius = 16             # 8–48
bfsVerticalRange = 16            # 4–32
combatSearchHorizontal = 10      # 5–30
combatSearchVertical = 5         # 2–15
breakCloseEnoughHorizontal = 3   # 1–6
breakCloseEnoughAbove = 3        # 2–5
breakCloseEnoughBelow = 2        # 1–3
oreAlertMinDist = 6              # 2–32
adjacentOreSearchH = 3           # 1–6
adjacentOreSearchV = 2           # 1–4
ceilingScanYStart = 3            # 1–8
ceilingScanYEnd = 16             # 8–24
ceilingScanHorizontal = 1        # 0–2
wanderRadiusH = 12               # 4–32
wanderRadiusV = 4                # 2–16
torchSearchDepth = 3             # 1–5

[mining.perception]              # 感知控制 (8项)
sniffRadiusHorizontalLevel0 = 1  # Lv0 水平嗅探  1–5
sniffRadiusHorizontalLevel1 = 2  # Lv1 水平嗅探  1–5
sniffRadiusHorizontalLevel2 = 3  # Lv2 水平嗅探  1–5
sniffRadiusHorizontalLevel3 = 4  # Lv3 水平嗅探  1–5
sniffRadiusVerticalLevel0 = 1    # Lv0 垂直嗅探  1–5
sniffRadiusVerticalLevel1 = 2    # Lv1 垂直嗅探  1–5
sniffRadiusVerticalLevel2 = 3    # Lv2 垂直嗅探  1–5
sniffRadiusVerticalLevel3 = 4    # Lv3 垂直嗅探  1–5
```

---

## 3. Cloth Config SubCategory

```java
// 入口
@EventBusSubscriber(modid = "mininglittlemaid", value = Dist.CLIENT, bus = Bus.GAME)

// 主分类
ConfigCategory mining = event.getRoot().getOrCreateCategory(Component.translatable("section.mining"));

// 子分类
SubCategoryBuilder sub = entryBuilder.startSubCategory(Component.translatable("section.cooldown"));
sub.setExpanded(false);  // 默认折叠
sub.add(entryBuilder.startIntSlider(...).build());
mining.addEntry(sub.build());
```

### GUI 结构

```
┌─ 采矿 ──────────────────────┐
│  矿脉连锁最大块数           │
│  火把放置亮度阈值           │
│  启用 Debug 日志            │
│  ▸ 冷却设置  (6项)          │
│  ▸ 检测频率  (6项)          │
│  ▸ 范围控制  (16项)         │
│  ▸ 感知控制  (8项)          │
└─────────────────────────────┘
```

### Cloth Config 显示单位

| 类型 | 转换公式 | 示例 |
|------|---------|------|
| 秒 | `get() / 20` / `set(val * 20)` | 冷却、频率 |
| 分 | `get() / 1200` / `set(val * 1200)` | 长时间冷却（通知） |
| 格 | `get()` / `set(val)` 直接使用 | 范围、距离、深度 |

---

## 4. Config.java 编写模板

```java
public static final ModConfigSpec.IntValue MY_FIELD;

static {
    ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    
    builder.push("mining")
            .translation("config.modid.section.mining");
    // ... 主分类条目 ...
    
    builder.push("cooldown")
            .translation("config.modid.section.cooldown");
    MY_FIELD = builder
            .comment("描述")
            .defineInRange("myField", 100, 20, 600);
    builder.pop();  // pop cooldown
    
    builder.pop(2); // pop mining
    SPEC = builder.build();
}
```

> `push("cooldown")` 在 `push("mining")` 内部 → TOML 生成 `[mining.cooldown]`

---

## 5. MiningClothConfig.java 模板

```java
@EventBusSubscriber(modid = "mininglittlemaid", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class MiningClothConfig {
    @SubscribeEvent
    public static void onAddClothConfig(AddClothConfigEvent event) {
        ConfigEntryBuilder eb = event.getEntryBuilder();
        ConfigCategory main = event.getRoot().getOrCreateCategory(translatable("section.mining"));

        // 直接条目
        main.addEntry(eb.startIntSlider(translatable("key"), Config.FIELD.get(), min, max)
                .setDefaultValue(default)
                .setSaveConsumer(val -> Config.FIELD.set(val))
                .build());

        // 子分类
        SubCategoryBuilder sub = eb.startSubCategory(translatable("section.cooldown"));
        sub.setExpanded(false);
        sub.add(eb.startIntSlider(translatable("key2"), Config.FIELD2.get(), min, max)
                .setDefaultValue(default)
                .setSaveConsumer(val -> Config.FIELD2.set(val))
                .build());
        main.addEntry(sub.build());
    }
}
```

### 常用条目类型

| 构建方法 | 用途 | 参数 |
|---------|------|------|
| `startIntSlider(label, current, min, max)` | 整数滑块 | `.setDefaultValue()` + `.setSaveConsumer()` |
| `startBooleanToggle(label, current)` | 布尔开关 | 同上 |

---

## 6. 服务端安全

```java
@EventBusSubscriber(modid = "mininglittlemaid", value = Dist.CLIENT, bus = Bus.GAME)
```

- `value = Dist.CLIENT` 确保服务端不加载此类 → 不触发 Cloth Config import
- TLM 内置 Cloth Config，客户端无需额外安装
- **不需要**在 `neoforge.mods.toml` 中声明 Cloth Config 依赖

---

## 7. Debug 日志

```java
// Config.java 中
public static void debugLog(Logger logger, String message, Object... params) {
    if (DEBUG_LOGGING.get()) {
        logger.debug(message, params);
    }
}

// 使用
Config.debugLog(LOGGER, "BFS started, radius={}", radius);
```

- 前提：`enableDebugLog = true`（Cloth Config 开关，**实时生效无需重启**）
- 所有 AI 决策点均已覆盖
