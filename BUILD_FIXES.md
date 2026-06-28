# FishBuddy Android — 编译排错全记录

> 2026-06-27

记录了从项目无法编译到成功生成 APK 的完整排查过程。

---

## 最终根因

**[AppDatabase.kt](app/src/main/java/com/fishbuddy/app/data/local/AppDatabase.kt) 缺少 `import androidx.room.RoomDatabase`**

```diff
  import androidx.room.Database
  import androidx.room.Room
+ import androidx.room.RoomDatabase
```

代码使用了 `RoomDatabase()` 作为父类但未导入该类。Kotlin 编译器无法解析 → KSP stub 生成缺少父类 → Room 注解处理器找不到类型 → `MissingType` 错误。

---

## 错误 1：Gradle `pluginManagement` 重复

```
Unexpected `pluginManagement` block found. Only one `pluginManagement` block is allowed per script.
```

**原因：** `settings.gradle.kts` 有 2 个 `pluginManagement` 块，且丢失了 `dependencyResolutionManagement`。

**修复：** 合并为一个 `pluginManagement`，恢复 `dependencyResolutionManagement`。

---

## 错误 2：Gradle Wrapper JAR 损坏（Codemagic）

```
Error: Could not find or load main class org.gradle.wrapper.GradleWrapperMain
```

**原因：** Git CRLF 转换损坏了 `gradle-wrapper.jar` 二进制文件。

**修复：**
- 添加 `.gitattributes`：`*.jar binary`
- `codemagic.yaml` 中改用 `gradle wrapper --gradle-version 8.9` 重新生成

---

## 错误 3：Room KSP `MissingType`（核心问题，排查最久）

```
e: [ksp] [MissingType]: Element 'com.fishbuddy.app.data.local.AppDatabase'
   references a type that is not present
```

**排查过程（全部无效，仅记录）：**
- ❌ Room 版本切换（2.6.1 ↔ 2.7.0）
- ❌ Kotlin 版本切换（2.1.0 ↔ 2.0.21）
- ❌ 移除 `@TypeConverters`
- ❌ 移除实体中 `equals()`/`hashCode()`
- ❌ 移除 `ByteArray` 字段
- ❌ 移除 `Flow` 返回类型
- ❌ 全量清缓存
- ❌ 切换到 kapt（结果相同错误 + JVM 崩溃）
- ❌ 极简实体测试（单 Int 主键仍失败）

**最终发现：** 通过 kapt 的 Java stub 输出发现生成的 `AppDatabase.java` 不继承 `RoomDatabase`，追溯到缺少 import。

---

## 错误 4：JVM Native Memory 崩溃（kapt）

```
Native memory allocation (mmap) failed. Out of Memory Error.
```

**原因：** kapt 内存消耗大；`-Xmx4096m` 挤占了 Native 内存空间。

**修复：** 切回 KSP（更轻量），降 `-Xmx` 到 1024m。

---

## 错误 5：KSP2 `unexpected jvm signature V`

**原因：** KSP2 的已知 bug，处理 Room 注解时报 JVM 字节码签名错误。

**修复：** `ksp.useKSP2=false`

---

## 错误 6：Compose `Unresolved reference 'background'`

**原因：** 缺少 `import androidx.compose.foundation.background`

**修复：** 在 `SpeciesDetailSheet.kt` 和 `AnalysisResultScreen.kt` 中添加 import。

---

## 错误 7：CameraX `bindToLifecycle` Internal API

**原因：** `ProcessCameraProvider.bindToLifecycle()` 是内部 API，外部不可访问。

**修复：** 删除错误的 `bindToLifecycle` 调用。

---

## 错误 8：SpotsScreen `CameraPosition` 未导入

**原因：** 缺少 `import com.google.android.gms.maps.model.CameraPosition`

**修复：** 添加 import。

---

## 最终可用配置

```properties
# gradle.properties
android.useAndroidX=true
kotlin.code.style=official
org.gradle.jvmargs=-Xmx1024m -Xms256m
kotlin.daemon.jvmargs=-Xmx512m
org.gradle.parallel=false
org.gradle.workers.max=1
ksp.useKSP2=false
```

```kotlin
// build.gradle.kts (root)
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
}
```

```kotlin
// app/build.gradle.kts (Room 部分)
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")
```
