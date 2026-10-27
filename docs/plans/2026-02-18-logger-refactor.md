# Logger Refactor Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Simplify OTG's logging system — merge 3 duplicated implementations into 1, add SLF4J-style `{}` placeholders with lazy formatting, replace 8-boolean `init()` with `EnumSet<LogCategory>`, fix LogLevel ordering, fix known bugs.

**Architecture:** `ILogger` interface stays as the contract. `Logger` abstract class gets deleted — its logic merges into `OTGLogger`. `OTGLog` static facade stays but gets simplified. A dual-mode formatter handles both `{}` (new) and `%s` (legacy) so callers can be migrated incrementally. Phase 1 changes internals (backward-compatible), Phase 2 migrates callers.

**Tech Stack:** Java 21, Log4j2 (via MC), EnumSet, no new dependencies.

**Worktree:** `.worktrees/1.21.1-logger-refactor/` (branch `1.21.1-logger-refactor` off `1.21.1`)

**IMPORTANT:** No automated tests exist. Verification is `./gradlew build` producing both JARs in `build/distributions/`.

---

## Scope

| Metric | Count |
|--------|-------|
| Total OTGLog call sites | ~416 |
| Files with logging | ~80 |
| `OTGLog.log()` (old verbose API) | ~140 (33 files) |
| `OTGLog.info/warn/error/fatal()` (convenience) | ~106 (25 files) |
| `getLogCategoryEnabled()` guards | ~81 (22 files) |
| Direct `ILogger` calls (not via OTGLog) | ~28 (12 files) |
| `OTG.log()` (duplicate static entry) | ~14 (6 files) |

## Known Bugs to Fix

1. **ILogger vs Logger param name mismatch** — `ILogger.init()` has `logPerformance` at pos 5, `Logger.init()` has `logBiomeRegistry` at pos 5. Works by coincidence (OTGEngine call also "swaps" them, so two wrongs make a right). `EnumSet` eliminates this entire class of bugs.
2. **Broken String.format calls** — 5 files have `String.format("Exception: ", (Object[])e.getStackTrace())` with no `%s` placeholder. Stack trace silently dropped. Files: `FileSettingsReaderBO4.java:294,304`, `FileSettingsWriterBO4.java:73,102`, `FileSettingsWriter.java:62`, `FileSettingsReader.java:39`.
3. **`OTG.log()` logs then throws** — `OTG.java:51-62`: logs the message, then checks if Engine is null and throws. Successful log + exception = nonsensical.

---

## Phase 1: Core Refactor (backward-compatible internals)

### Task 1: Fix LogLevel enum ordering

Current ordering is `FATAL(0), ERROR(1), WARN(2), INFO(3)` — counterintuitive, causes different comparison logic across implementations. Fix to `INFO(0), WARN(1), ERROR(2), FATAL(3)`.

**Files:**
- Modify: `common/common-util/src/main/java/com/pg85/otg/util/logging/LogLevel.java`
- Modify: `platforms/shared/src/main/java/com/pg85/otg/shared/util/OTGLogger.java` — fix comparison
- Modify: `common/common-util/src/main/java/com/pg85/otg/util/OTGLog.java` — fix BasicLogger comparison
- Modify: `common/common-test/src/main/java/com/pg85/otg/test/engine/TestLogger.java` — fix comparison
- Modify: `common/common-util/src/main/java/com/pg85/otg/util/logging/Logger.java` — not yet deleted, fix comparison for now

**Step 1: Reverse LogLevel enum**

```java
// LogLevel.java
package com.pg85.otg.util.logging;

public enum LogLevel {
    INFO,
    WARN,
    ERROR,
    FATAL
}
```

**Step 2: Fix all level comparisons**

Every implementation compares levels differently. Unify to: `level.ordinal() >= minimumLevel.ordinal()` means "log if message severity meets threshold."

In `OTGLogger.log()` (line 17), currently:
```java
if (this.minimumLevel.compareTo(level) < 0) return;
```
Change to:
```java
if (level.ordinal() < this.minimumLevel.ordinal()) return;
```

In `OTGLog.BasicLogger.log()` (line 122), currently:
```java
if (this.level.ordinal() <= level.ordinal())
```
Change to:
```java
if (level.ordinal() >= this.level.ordinal())
```

In `TestLogger.log()` (line 44), currently:
```java
if (level.ordinal() > minLevel.ordinal()) return;
```
Change to:
```java
if (level.ordinal() < minLevel.ordinal()) return;
```

In `Logger.java` — no comparison logic in `log()` (abstract), so no change needed here.

**Step 3: Fix LogLevels mapping**

Check `common/common-util/src/main/java/com/pg85/otg/constants/settings/LogLevels.java` — it maps config strings to LogLevel. Should now be:
- `Off` → `LogLevel.ERROR` (shows ERROR + FATAL)
- `Quiet` → `LogLevel.WARN` (shows WARN + ERROR + FATAL)
- `Standard` → `LogLevel.INFO` (shows everything)

These mappings should still work since the comparison logic is "log if `level >= minLevel`" and `ERROR(2) >= ERROR(2)` = true, `WARN(1) >= ERROR(2)` = false. Verify this is correct.

**Step 4: Verify build**

```bash
cd .worktrees/1.21.1-logger-refactor && ./gradlew build
```

**Step 5: Commit**

```
build: fix LogLevel enum ordering — INFO < WARN < ERROR < FATAL
```

---

### Task 2: Add message formatting utility

Create a static formatter that handles both `{}` (SLF4J-style) and `%s` (legacy String.format) — this allows incremental migration of callers.

**Files:**
- Create: `common/common-util/src/main/java/com/pg85/otg/util/logging/LogFormatter.java`

**Step 1: Create LogFormatter**

```java
package com.pg85.otg.util.logging;

/**
 * Formats log messages with parameter substitution.
 * Supports both {} (SLF4J-style, preferred) and %s (legacy String.format).
 * If message contains {}, uses {} replacement. Otherwise falls back to String.format.
 */
public final class LogFormatter {
    private LogFormatter() {}

    public static String format(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        if (message.contains("{}")) {
            return replaceBraces(message, args);
        }
        try {
            return String.format(message, args);
        } catch (java.util.IllegalFormatException e) {
            // Broken format string (e.g., no %s but args passed) — return message + args
            return message + " " + java.util.Arrays.toString(args);
        }
    }

    private static String replaceBraces(String message, Object[] args) {
        StringBuilder sb = new StringBuilder(message.length() + 64);
        int argIdx = 0;
        int start = 0;
        int idx;
        while ((idx = message.indexOf("{}", start)) != -1 && argIdx < args.length) {
            sb.append(message, start, idx);
            sb.append(args[argIdx++]);
            start = idx + 2;
        }
        sb.append(message, start, message.length());
        return sb.toString();
    }
}
```

Note: The `IllegalFormatException` catch fixes the broken `String.format("Exception: ", stackTrace)` calls (bug #2) — instead of silently dropping args, they'll be appended.

**Step 2: Verify build**

```bash
cd .worktrees/1.21.1-logger-refactor && ./gradlew build
```

**Step 3: Commit**

```
feat: add LogFormatter with {} and %s support for logging
```

---

### Task 3: Rewrite ILogger interface

Replace the 8-boolean `init()` with `EnumSet<LogCategory>`. Add `isEnabled()`. Change default methods to use `LogFormatter` with lazy formatting (only format when enabled).

**Files:**
- Modify: `common/common-util/src/main/java/com/pg85/otg/interfaces/ILogger.java`

**Step 1: Rewrite ILogger**

```java
package com.pg85.otg.interfaces;

import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogFormatter;
import com.pg85.otg.util.logging.LogLevel;

import java.util.EnumSet;

public interface ILogger {

    void init(LogLevel level, EnumSet<LogCategory> enabledCategories, String logPresets);

    void log(LogLevel level, LogCategory category, String message);

    boolean isEnabled(LogLevel level, LogCategory category);

    boolean canLogForPreset(String presetFolderName);

    // --- Convenience: with category ---

    default void info(LogCategory category, String message, Object... args) {
        if (isEnabled(LogLevel.INFO, category)) {
            log(LogLevel.INFO, category, LogFormatter.format(message, args));
        }
    }

    default void warn(LogCategory category, String message, Object... args) {
        if (isEnabled(LogLevel.WARN, category)) {
            log(LogLevel.WARN, category, LogFormatter.format(message, args));
        }
    }

    default void error(LogCategory category, String message, Object... args) {
        if (isEnabled(LogLevel.ERROR, category)) {
            log(LogLevel.ERROR, category, LogFormatter.format(message, args));
        }
    }

    default void fatal(LogCategory category, String message, Object... args) {
        if (isEnabled(LogLevel.FATAL, category)) {
            log(LogLevel.FATAL, category, LogFormatter.format(message, args));
        }
    }

    // --- Convenience: implicit MAIN category ---

    default void info(String message, Object... args) {
        info(LogCategory.MAIN, message, args);
    }

    default void warn(String message, Object... args) {
        warn(LogCategory.MAIN, message, args);
    }

    default void error(String message, Object... args) {
        error(LogCategory.MAIN, message, args);
    }

    default void fatal(String message, Object... args) {
        fatal(LogCategory.MAIN, message, args);
    }

    // --- Exception logging ---

    default void error(LogCategory category, String message, Exception e) {
        if (isEnabled(LogLevel.ERROR, category)) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            log(LogLevel.ERROR, category, message + "\n" + sw);
        }
    }

    default void error(String message, Exception e) {
        error(LogCategory.MAIN, message, e);
    }
}
```

Key changes:
- `init()` takes `EnumSet<LogCategory>` instead of 8 booleans — eliminates positional parameter bugs
- `isEnabled()` replaces `getLogCategoryEnabled()` — combined level + category check
- Default methods do lazy formatting — only call `LogFormatter.format()` when `isEnabled()` passes
- `error(category, message, exception)` combines the common "log + printStackTrace" pattern into one call
- Removed standalone `printStackTrace()` — use `error(cat, msg, exception)` instead

**Step 2: Verify build** — will NOT compile yet (callers still use old API). That's expected.

---

### Task 4: Rewrite OTGLogger — merge Logger abstract

Delete `Logger.java`. Move its field storage and filtering logic directly into `OTGLogger`.

**Files:**
- Modify: `platforms/shared/src/main/java/com/pg85/otg/shared/util/OTGLogger.java`
- Delete: `common/common-util/src/main/java/com/pg85/otg/util/logging/Logger.java`

**Step 1: Rewrite OTGLogger**

```java
package com.pg85.otg.shared.util;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import org.apache.logging.log4j.LogManager;

import java.util.EnumSet;
import java.util.Locale;

public class OTGLogger implements ILogger {
    private final org.apache.logging.log4j.Logger logger =
            LogManager.getLogger(Constants.MOD_ID_SHORT.toUpperCase(Locale.ROOT));

    private LogLevel minLevel = LogLevel.INFO;
    private final EnumSet<LogCategory> enabledCategories = EnumSet.of(LogCategory.MAIN);
    private String logPresets = "all";

    @Override
    public void init(LogLevel level, EnumSet<LogCategory> enabledCategories, String logPresets) {
        this.minLevel = level;
        this.enabledCategories.clear();
        this.enabledCategories.add(LogCategory.MAIN); // MAIN is always enabled
        this.enabledCategories.addAll(enabledCategories);
        this.logPresets = logPresets;
    }

    @Override
    public boolean isEnabled(LogLevel level, LogCategory category) {
        return level.ordinal() >= minLevel.ordinal() && enabledCategories.contains(category);
    }

    @Override
    public boolean canLogForPreset(String presetFolderName) {
        return "all".equalsIgnoreCase(logPresets) || logPresets.equalsIgnoreCase(presetFolderName);
    }

    @Override
    public void log(LogLevel level, LogCategory category, String message) {
        if (!isEnabled(level, category)) return;

        String taggedMessage = category.getLogTag() + " " + message;
        switch (level) {
            case FATAL -> logger.fatal(taggedMessage);
            case ERROR -> logger.error(taggedMessage);
            case WARN -> logger.warn(taggedMessage);
            case INFO -> logger.info(taggedMessage);
        }
    }
}
```

**Step 2: Delete Logger.java**

```bash
rm common/common-util/src/main/java/com/pg85/otg/util/logging/Logger.java
```

---

### Task 5: Rewrite OTGLog static facade

Simplify `BasicLogger` fallback, update static methods to match new `ILogger` API, keep backward compatibility for `OTGLog.log(LogLevel, LogCategory, String)`.

**Files:**
- Modify: `common/common-util/src/main/java/com/pg85/otg/util/OTGLog.java`

**Step 1: Rewrite OTGLog**

```java
package com.pg85.otg.util;

import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogFormatter;
import com.pg85.otg.util.logging.LogLevel;

import java.util.EnumSet;

public final class OTGLog {
    private static ILogger logger = new FallbackLogger();

    public static void setLogger(ILogger logger) {
        OTGLog.logger = logger;
    }

    public static ILogger getLogger() {
        return OTGLog.logger;
    }

    // --- Core ---

    public static void log(LogLevel level, LogCategory category, String message) {
        logger.log(level, category, message);
    }

    public static boolean isEnabled(LogLevel level, LogCategory category) {
        return logger.isEnabled(level, category);
    }

    public static boolean canLogForPreset(String presetFolderName) {
        return logger.canLogForPreset(presetFolderName);
    }

    // --- Convenience: with category ---

    public static void info(LogCategory category, String message, Object... args) {
        logger.info(category, message, args);
    }

    public static void warn(LogCategory category, String message, Object... args) {
        logger.warn(category, message, args);
    }

    public static void error(LogCategory category, String message, Object... args) {
        logger.error(category, message, args);
    }

    public static void fatal(LogCategory category, String message, Object... args) {
        logger.fatal(category, message, args);
    }

    // --- Convenience: implicit MAIN ---

    public static void info(String message, Object... args) {
        logger.info(message, args);
    }

    public static void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public static void error(String message, Object... args) {
        logger.error(message, args);
    }

    public static void fatal(String message, Object... args) {
        logger.fatal(message, args);
    }

    // --- Exception logging ---

    public static void error(LogCategory category, String message, Exception e) {
        logger.error(category, message, e);
    }

    public static void error(String message, Exception e) {
        logger.error(message, e);
    }

    // --- Deprecated: remove after full migration ---

    /** @deprecated Use {@link #isEnabled(LogLevel, LogCategory)} */
    @Deprecated
    public static boolean getLogCategoryEnabled(LogCategory category) {
        return logger.isEnabled(LogLevel.INFO, category);
    }

    /** @deprecated Use {@link #error(LogCategory, String, Exception)} */
    @Deprecated
    public static void printStackTrace(LogLevel level, LogCategory category, Exception e) {
        logger.error(category, "Exception", e);
    }

    // --- Fallback logger (pre-engine startup) ---

    private static class FallbackLogger implements ILogger {
        private LogLevel minLevel = LogLevel.INFO;

        @Override
        public void init(LogLevel level, EnumSet<LogCategory> enabledCategories, String logPresets) {
            this.minLevel = level;
        }

        @Override
        public boolean isEnabled(LogLevel level, LogCategory category) {
            return level.ordinal() >= minLevel.ordinal();
        }

        @Override
        public boolean canLogForPreset(String presetFolderName) {
            return true;
        }

        @Override
        public void log(LogLevel level, LogCategory category, String message) {
            if (!isEnabled(level, category)) return;
            if (level.ordinal() >= LogLevel.WARN.ordinal()) {
                System.err.println("[OTG] " + level.name() + " " + category.getLogTag() + " " + message);
            } else {
                System.out.println("[OTG] " + level.name() + " " + category.getLogTag() + " " + message);
            }
        }
    }
}
```

---

### Task 6: Update OTGEngine and PluginConfig

Replace 8-boolean `init()` call with `EnumSet`. Build the set from `PluginConfig` fields.

**Files:**
- Modify: `common/common-core/src/main/java/com/pg85/otg/OTGEngine.java:85-95`
- Modify: `common/common-core/src/main/java/com/pg85/otg/config/PluginConfigBase.java` — add `getEnabledLogCategories()` method

**Step 1: Add helper to PluginConfigBase**

Find `PluginConfigBase` and add:

```java
public EnumSet<LogCategory> getEnabledLogCategories() {
    EnumSet<LogCategory> categories = EnumSet.of(LogCategory.MAIN);
    if (logCustomObjects) categories.add(LogCategory.CUSTOM_OBJECTS);
    if (logStructurePlotting) categories.add(LogCategory.STRUCTURE_PLOTTING);
    if (logConfigs) categories.add(LogCategory.CONFIGS);
    if (logBiomeRegistry) categories.add(LogCategory.BIOME_REGISTRY);
    if (logPerformance) categories.add(LogCategory.PERFORMANCE);
    if (logDecoration) categories.add(LogCategory.DECORATION);
    if (logMobs) categories.add(LogCategory.MOBS);
    return categories;
}
```

Add imports: `java.util.EnumSet`, `com.pg85.otg.util.logging.LogCategory`.

**Step 2: Simplify OTGEngine.onStart() init call**

Replace lines 85-95:
```java
this.logger.init(
    this.pluginConfig.getLogLevel().getLevel(),
    this.pluginConfig.getEnabledLogCategories(),
    this.pluginConfig.logPresets()
);
```

**Step 3: Remove individual boolean getters from IPluginConfig** (if no longer needed outside init).

Check if `logCustomObjects()`, `logStructurePlotting()` etc. are used anywhere outside `PluginConfig.readConfigSettings()` and the old `init()` call. If not, they can become private in `PluginConfigBase`. If they're exposed via `IPluginConfig`, leave them for now and clean up later.

---

### Task 7: Update TestLogger

**Files:**
- Modify: `common/common-test/src/main/java/com/pg85/otg/test/engine/TestLogger.java`

**Step 1: Rewrite TestLogger to match new ILogger**

```java
package com.pg85.otg.test.engine;

import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;

public class TestLogger implements ILogger {
    private LogLevel minLevel = LogLevel.INFO;
    private EnumSet<LogCategory> enabledCategories = EnumSet.of(LogCategory.MAIN, LogCategory.CONFIGS);

    @Override
    public void init(LogLevel level, EnumSet<LogCategory> enabledCategories, String logPresets) {
        this.minLevel = level;
        this.enabledCategories = EnumSet.of(LogCategory.MAIN);
        this.enabledCategories.addAll(enabledCategories);
    }

    @Override
    public boolean isEnabled(LogLevel level, LogCategory category) {
        return level.ordinal() >= minLevel.ordinal() && enabledCategories.contains(category);
    }

    @Override
    public boolean canLogForPreset(String presetFolderName) {
        return true;
    }

    @Override
    public void log(LogLevel level, LogCategory category, String message) {
        if (!isEnabled(level, category)) return;
        System.err.println(level.name() + " " + category.name() + " " + message);
    }
}
```

---

### Task 8: Fix OTG.java — remove duplicate logger, simplify

**Files:**
- Modify: `common/common-core/src/main/java/com/pg85/otg/OTG.java`

**Step 1: Remove OTG.logger field and OTG.log() methods**

`OTG.log()` is a duplicate of `OTGLog.log()` pointing to the same object. Remove it. Callers will be migrated in Phase 2.

But first: check if removing `OTG.getLogger()` (Lombok `@Getter`) breaks anything. Research showed some callers use `OTG.getEngine().getLogger()` — those go through `OTGEngine.getLogger()`, not `OTG.getLogger()`. Remove `OTG.logger` field and the `@Getter` on it.

Replace `OTG.log()` methods with deprecation stubs that delegate to `OTGLog`:

```java
/** @deprecated Use OTGLog.log() directly */
@Deprecated
public static void log(LogLevel logLevel, LogCategory logCategory, String message) {
    OTGLog.log(logLevel, logCategory, message);
}

/** @deprecated Use OTGLog.info() directly */
@Deprecated
public static void log(String message) {
    OTGLog.info(message);
}
```

Remove `logger` field and the `OTG.startEngine()` line `logger = Engine.getLogger()`.

**Step 2: Verify build**

```bash
cd .worktrees/1.21.1-logger-refactor && ./gradlew build
```

At this point, compilation may fail due to callers of old `ILogger` methods. Some callers use:
- `getLogCategoryEnabled()` — now `isEnabled(level, category)`
- `printStackTrace()` — now `error(category, message, exception)`
- Direct `logger.log()` still works (signature unchanged)

Fix compile errors as they arise — most will be in `platforms/shared/` and `common/` code that calls `getLogCategoryEnabled()`.

**Step 3: Commit**

```
refactor: merge Logger into OTGLogger, simplify ILogger init, add lazy {} formatting
```

---

## Phase 2: Caller Migration

### Task 9: Migrate `getLogCategoryEnabled()` → `isEnabled()`

~81 call sites across 22 files. Two patterns:

**Pattern A** (most common — guard + log pair):
```java
// Before:
if (OTGLog.getLogCategoryEnabled(LogCategory.BIOME_REGISTRY)) {
    OTGLog.getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "message");
}
// After (guard still needed for string concatenation):
if (OTGLog.isEnabled(LogLevel.INFO, LogCategory.BIOME_REGISTRY)) {
    OTGLog.info(LogCategory.BIOME_REGISTRY, "message");
}
```

**Pattern B** (guard + log with expensive string building — keep the guard):
```java
// Before:
if (OTGLog.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS)) {
    OTGLog.log(LogLevel.INFO, LogCategory.CUSTOM_OBJECTS, "Placed " + obj.getName() + " at " + x + "," + z);
}
// After (use {} to avoid concatenation, remove guard):
OTGLog.info(LogCategory.CUSTOM_OBJECTS, "Placed {} at {},{}", obj.getName(), x, z);
```

If the guarded block only does logging (no side effects), the guard can be removed when switching to `{}` placeholders — the `ILogger` default method checks `isEnabled()` before formatting.

If the guarded block computes expensive values ONLY for logging, keep the guard with `isEnabled()`.

**Files (heaviest first):**
- `common/common-customobject/.../structures/bo4/BO4CustomStructure.java` — 19 guards
- `common/common-customobject/.../CustomObjectCollection.java` — 9 guards
- `common/common-customobject/.../bo4/BO4Config.java` — 8 guards
- `common/common-customobject/.../bo4/BO4.java` — 8 guards
- `common/common-core/.../gen/OTGChunkDecorator.java` — 6 guards
- `platforms/shared/.../gen/SharedWorldGenRegion.java` — 9 guards
- Remaining 16 files — 1-4 guards each

**Commit after each module** (common-customobject, common-core, common-util, platforms/shared):

```
refactor(logging): migrate getLogCategoryEnabled → isEnabled in <module>
```

---

### Task 10: Migrate `OTGLog.log(LogLevel, LogCategory, String)` → convenience methods

~140 call sites across 33 files. Mechanical transformation:

```java
// Before:
OTGLog.log(LogLevel.INFO, LogCategory.MAIN, "message");
// After:
OTGLog.info(LogCategory.MAIN, "message");

// Before:
OTGLog.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "error: " + detail);
// After:
OTGLog.error(LogCategory.CUSTOM_OBJECTS, "error: {}", detail);

// Before (with string concat):
OTGLog.log(LogLevel.INFO, LogCategory.STRUCTURE_PLOTTING,
    "Plotted " + count + " branches for " + name + " in " + ms + "ms");
// After:
OTGLog.info(LogCategory.STRUCTURE_PLOTTING,
    "Plotted {} branches for {} in {}ms", count, name, ms);
```

**Heaviest files (handle first):**
- `BO4CustomStructure.java` — 25 calls
- `CustomStructureFileManager.java` — 13 calls
- `OTGChunkDecorator.java` — 10 calls
- `CustomObjectCollection.java` — 10 calls
- `BO4Config.java` — 9 calls
- `BO4.java` — 9 calls
- `SharedWorldGenRegion.java` — 9 calls

**Commit per module:**

```
refactor(logging): migrate OTGLog.log() → convenience methods in <module>
```

---

### Task 11: Migrate `OTGLog.printStackTrace()` → `error(cat, msg, exception)`

~31 call sites across 9 files. Most follow the pattern:

```java
// Before:
OTGLog.error(LogCategory.X, "message: %s", e.getMessage());
OTGLog.printStackTrace(LogLevel.ERROR, LogCategory.X, e);

// After:
OTGLog.error(LogCategory.X, "message", e);
```

This combines the two calls into one and includes the full stack trace.

**Files:**
- `CustomStructureFileManager.java` — 12 calls
- `BO4Config.java` — 3 calls
- `SpawnCommand.java` — 2 calls
- `ExportCommand.java` — 2 calls
- `NBTHelper.java` — 2 calls
- `ObjectCreator.java` — 2 calls
- `BO4Data.java` — 1 call
- `ExportBO4DataCommand.java` — 1 call
- `SnapshotCli.java` — 6 calls

**Commit:**

```
refactor(logging): merge log+printStackTrace pairs into error(cat, msg, exception)
```

---

### Task 12: Migrate `OTG.log()` → `OTGLog`

~14 active call sites across 6 files:

| File | Calls | Notes |
|------|-------|-------|
| `SharedOTGChunkGenerator.java` | 6 | Timing logs with `String.format` |
| `OTGPlugin.java` (Fabric) | 1 | Startup message |
| `OTGPlugin.java` (NeoForge) | 1 | Startup message |
| `FabricEngine.java` | 1 | Jar path warning |
| `NeoForgeEngine.java` | 1 | Jar path warning |
| `BiomeRegistrar.java` | 1 | Error with concat |

```java
// Before:
OTG.log(String.format("[OTG-TIMING] fillFromNoise #%d avg=%.1fms", count, avg));
// After:
OTGLog.info(LogCategory.PERFORMANCE, "fillFromNoise #{} avg={}ms", count, String.format("%.1f", avg));

// Before:
OTG.log("OTG Engine started, presets loaded");
// After:
OTGLog.info("OTG Engine started, presets loaded");
```

Note: for `%.1f` float formatting, keep `String.format()` for the individual value since `{}` doesn't support format specifiers.

**Commit:**

```
refactor(logging): replace OTG.log() with OTGLog calls
```

---

### Task 13: Migrate `%s` → `{}` in format strings

~60 call sites that pass `%s` format strings through convenience methods. After Task 2 (LogFormatter), both styles work, but `{}` is preferred for consistency and because it matches the SLF4J convention MC modders are familiar with.

Mechanical find-and-replace per file. For `%d` (used in ~2 places like `MinecraftObjectFunction.java`), keep `%d` since `{}` calls `toString()` and produces the same output for integers.

**Commit:**

```
refactor(logging): migrate %s format strings to {} placeholders
```

---

### Task 14: Fix broken String.format calls (bug #2)

5 files with `String.format("Exception message: ", (Object[])e.getStackTrace())` — no placeholder, stack trace silently dropped.

**Files:**
- `common/common-customobject/.../config/io/FileSettingsReaderBO4.java:294,304`
- `common/common-customobject/.../config/io/FileSettingsWriterBO4.java:73,102`
- `common/common-util/.../config/io/FileSettingsWriter.java:62`
- `common/common-util/.../config/io/FileSettingsReader.java:39`

These should use the new `error(category, message, exception)` pattern:

```java
// Before:
OTGLog.getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS,
    String.format("Exception when reading file: ", (Object[])e.getStackTrace()));
// After:
OTGLog.error(LogCategory.CONFIGS, "Exception when reading file", e);
```

**Commit:**

```
fix(logging): fix broken String.format calls that silently dropped stack traces
```

---

### Task 15: Remove deprecated methods and dead code

After all callers are migrated:

1. Remove `@Deprecated` methods from `OTGLog` (`getLogCategoryEnabled`, `printStackTrace`)
2. Remove `@Deprecated` methods from `OTG` (`log()`)
3. Remove `OTG.logger` field entirely
4. Remove unused `ILogger` method `getLogCategoryEnabled()` if no callers remain
5. Verify `Logger.java` is deleted (Task 4)

**Commit:**

```
refactor(logging): remove deprecated logging methods and dead code
```

---

### Task 16: Final build verification

```bash
cd .worktrees/1.21.1-logger-refactor && ./gradlew clean build
```

Expected:
- `build/distributions/otg-fabric-0.3.0-dev2.jar`
- `build/distributions/otg-neoforge-0.3.0-dev2.jar`

Verify no compilation warnings about deprecated usage remain (other than MC/Loom deprecations).
