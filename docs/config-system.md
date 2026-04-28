# MiningLittleMaid Config 系统技术文档

## 适用模组

基于 NeoForge 1.21.1 的 TouhouLittleMaid 附属模组

---

## 1. 整体架构

```
Config.java (ModConfigSpec 定义)
    ↓
@Mod 构造函数 → modContainer.registerConfig(Type, SPEC)
    ↓
运行时生成: config/mining_little_maid-common.toml
    ↓
游戏内可通过 "Mods → Mining Little Maid → Config" 修改
```

---

## 2. Config.java 完整模板

```java
package com.example.modid.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.Logger;

public class Config {
    // === Spec 入口（必须） ===
    public static final ModConfigSpec SPEC;

    // === 配置项声明 ===
    public static final ModConfigSpec.IntValue MAX_VEIN_SIZE;
    public static final ModConfigSpec.BooleanValue ENABLE_FEATURE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        // === 顶级注释 ===
        builder.comment("My Mod 配置");

        // === 分类 section（必须成对 push → pop） ===
        builder.push("mining")
                .translation("config.modid.section.mining");

        MAX_VEIN_SIZE = builder
                .comment("矿脉连锁最大块数（仅好感度等级 3 生效）")
                .defineInRange("maxVeinSize", 8, 2, 64);

        ENABLE_FEATURE = builder
                .comment("启用某功能（需重启生效）")
                .define("enableFeature", true);

        // === 分类结束 ===
        builder.pop();

        SPEC = builder.build();
    }

    // === 条件日志（可选） ===
    public static void debugLog(Logger logger, String message, Object... params) {
        if (ENABLE_FEATURE.get()) {
            logger.debug(message, params);
        }
    }
}
```

---

## 3. 配置值类型

| 方法 | 返回类型 | 用法 |
|------|---------|------|
| `.defineInRange(key, default, min, max)` | `IntValue` | 整数，自动约束范围 |
| `.define(key, default)` | `BooleanValue` | 布尔值 |
| `.defineInRange(key, default, min, max)` | `DoubleValue` | 浮点数（用 `Double` 参数） |
| `.define(key, default)` | `ConfigValue<String>` | 字符串 |
| `.defineList(key, default, validator)` | `ConfigValue<List<T>>` | 列表 |

---

## 4. 读取配置值

```java
// 在运行时读取（线程安全）
int veinSize = Config.MAX_VEIN_SIZE.get();
boolean enabled = Config.ENABLE_FEATURE.get();
```

**注意**：不要在 static 初始化块中调用 `.get()`，只能在 `static {}` 完成后读取。

---

## 5. 注册到 NeoForge

在 `@Mod` 主类的构造函数中：

```java
@Mod(MyMod.MOD_ID)
public class MyMod {
    public static final String MOD_ID = "my_mod";

    public MyMod(IEventBus modEventBus, ModContainer modContainer) {
        // 注册 COMMON 配置（客户端+服务端共用）
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        // 可选：SERVER 配置（仅服务端）
        // modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }
}
```

---

## 6. 配置文件类型对比

| 类型 | 文件 | 同步方式 | 适用场景 |
|------|------|---------|---------|
| `COMMON` | `modid-common.toml` | 客户端从服务端同步 | **推荐** 服务端管理，所有玩家统一 |
| `SERVER` | `modid-server.toml` | 仅服务端 | 服务端专属设置（备份间隔等） |
| `CLIENT` | `modid-client.toml` | 仅客户端 | 客户端显示设置（渲染等） |

> **本模组原则**：服务端管理的配置（`maxVeinSize` 等）放 `COMMON`；每只女仆的个性化设置（`显示挖矿提示` 等）放 NBT + 任务配置 Tab。

---

## 7. 分类 section（push/pop）

```java
builder.push("mining")                    // TOML 中生成 [mining] 头
        .translation("config.modid.section.mining");  // 分类标题翻译键
// ... 配置项 ...
builder.pop();                            // 必须与 push 配对
```

翻译键注册（语言文件）：
```json
{
    "config.modid.section.mining": "采矿"
}
```

---

## 8. TouhouLittleMaid 父模组配置参考

TouhouLittleMaid 的 `GeneralConfig` 分为 6 个 section：

| section | 翻译键 | 内容 |
|---------|--------|------|
| `[maid]` | `config.touhou_little_maid.maid` | 女仆基本行为 |
| `[chair]` | `config.touhou_little_maid.chair` | 椅子相关 |
| `[misc]` | `config.touhou_little_maid.misc` | 杂项 |
| `[vanilla]` | `config.touhou_little_maid.vanilla` | 原版交互 |
| `[render]` | `config.touhou_little_maid.render` | 渲染 |
| `[ai]` | *(无翻译)* | AI 相关 |

父模组游戏内配置 GUI 使用 **Cloth Config 2** (`MenuIntegration.java`)。

---

## 9. Debug/条件日志模式

```java
// Config.java 中定义
public static void debugLog(Logger logger, String message, Object... params) {
    if (DEBUG_LOGGING.get()) {
        logger.debug(message, params);
    }
}

// 业务代码中调用（用 { } 占位符避免字符串拼接）
Config.debugLog(LOGGER, "BFS search started, radius={}", radius);
```

---

## 10. 每只女仆的个性化设置（非 Config.java）

如需每只女仆独立配置，使用 **NBT 持久化** + **任务配置 Tab** 模式：

```java
// 写入（服务端）
maid.getPersistentData().putBoolean("my_key", true);

// 读取
boolean val = maid.getPersistentData().getBoolean("my_key");
// 注意：key 不存在时返回 false，用 contains 先判断默认值
boolean enabled = !maid.getPersistentData().contains("my_key")
        || maid.getPersistentData().getBoolean("my_key");
```

---

## 11. 完整文件清单

| 文件 | 职责 |
|------|------|
| `config/Config.java` | 定义 ModConfigSpec + 条件日志方法 |
| `lang/zh_cn.json` | 包含 `config.modid.section.*` 分类翻译键 |
| `YourMod.java` (@Mod) | `modContainer.registerConfig()` 注册入口 |
