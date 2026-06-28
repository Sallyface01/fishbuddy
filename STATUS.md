# FishBuddy Android — 项目状态

> 最后更新：2026-06-27（编译成功 🎉）

---

## 项目概述

FishBuddy（钓鱼助手）Android App，iOS 版本的 Kotlin/Jetpack Compose 移植。
- **主工作目录：** `android-fishbuddy-fixed`（已修复版本）
- **原始目录：** `android-fishbuddy`（已同步全部修复）
- **状态：** ✅ Debug APK 编译成功（59.9 MB）

---

## ⚙️ 最终工具链版本

| 组件 | 版本 |
|------|------|
| AGP | 8.7.3 |
| Kotlin | 2.1.0 |
| KSP | 2.1.0-1.0.29 |
| Room | 2.6.1 |
| Gradle | 8.9（本地 zip） |
| Compose BOM | 2024.12.01 |
| JDK | 17 |

---

## ✅ 全部修复清单（共 11 项）

### 🔑 核心 Bug — Room KSP MissingType

**根因：** [AppDatabase.kt](d:\code\Swift\android-fishbuddy-fixed\app\src\main\java\com\fishbuddy\app\data\local\AppDatabase.kt) 使用了 `RoomDatabase()` 但**缺少 `import androidx.room.RoomDatabase`**。
Kotlin 编译器无法解析 `RoomDatabase` 类 → KSP stub 生成失败 → Room 注解处理器报 `MissingType`。

**修复：** 添加 `import androidx.room.RoomDatabase` + 去掉 `@TypeConverters`（KSP2 兼容性）。

### 构建配置修复

| # | 文件 | 修复 |
|---|------|------|
| 1 | `settings.gradle.kts` | 合并重复 `pluginManagement` 块，恢复 `dependencyResolutionManagement`，添加阿里云镜像 |
| 2 | `gradle-wrapper.properties` | Gradle `9.0-milestone-1` → `8.9`；本地路径 `file:///d:/code/Gradle/gradle-8.9-bin.zip` |
| 3 | `gradle.properties` | `ksp.useKSP2=false`（避免 KSP2 的 `jvm signature V` 崩溃）；`-Xmx1024m`（防止 Native OOM） |
| 4 | `build.gradle.kts`（根） | Kotlin 2.1.0 + KSP 2.1.0-1.0.29 |
| 5 | `app/build.gradle.kts` | Room 2.6.1 + KSP（非 kapt）；去掉 `kapt` 配置块 |
| 6 | `local.properties` | 删除并加入 `.gitignore`（SDK 路径不提交） |
| 7 | `.gitignore` + `.gitattributes` | `*.jar binary` 防止 Git CRLF 损坏；排除构建产物 |
| 8 | `codemagic.yaml` | 用 `gradle wrapper --gradle-version 8.9` 重新生成 wrapper，避免 JAR 损坏 |

### Kotlin 源代码修复

| # | 文件 | 修复 |
|---|------|------|
| 9 | `AppDatabase.kt` | **添加 `import androidx.room.RoomDatabase`** + 去掉 `@TypeConverters` |
| 10 | `CameraService.kt` | 去掉错误的 `bindToLifecycle()` 调用（内部 API） |
| 11 | `SpeciesDetailSheet.kt` | 添加 `import androidx.compose.foundation.background` |
| 12 | `AnalysisResultScreen.kt` | 添加 `import androidx.compose.foundation.background` |
| 13 | `SpotsScreen.kt` | 添加 `import com.google.android.gms.maps.model.CameraPosition` |

---

## 🔧 关键经验教训

1. **`MissingType` 不一定是版本问题** — 排查了 Room/Kotlin/KSP/AGP 版本组合均无效，最终是一个缺失的 import
2. **kapt 内存消耗大** — 机器空闲内存不足时 kapt 会 OOM 崩溃，KSP 更轻量
3. **`ksp.useKSP2=true` 有 bug** — Kotlin 2.1.0 + KSP2 报 `unexpected jvm signature V`，需关闭
4. **堆内存太大会挤占 Native 内存** — `-Xmx4096m` 导致 JVM 崩溃，降到 1024m 反而通过

---

## 文件结构

```
android-fishbuddy-fixed/
├── STATUS.md              ← 本文件
├── BUILD_FIXES.md         ← 详细排错文档
├── build.gradle.kts       ← 插件版本声明
├── settings.gradle.kts    ← 仓库镜像 + 项目配置
├── codemagic.yaml         ← CI 构建配置
├── gradle.properties      ← ksp.useKSP2=false, -Xmx1024m
├── .gitignore
├── .gitattributes         ← *.jar binary
├── local.properties       ← SDK 路径（gitignored）
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties  ← 本地 file:/// 路径
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/data/   ← JSON 鱼种数据库
│       ├── res/           ← 中英双语资源
│       └── java/com/fishbuddy/app/
│           ├── FishBuddyApp.kt
│           ├── MainActivity.kt
│           ├── data/      ← Room 数据库层
│           ├── domain/    ← 领域模型
│           ├── service/   ← 相机/定位/天气/ML Kit
│           └── ui/        ← Compose UI（5 个 Tab）
└── docs/
```

## 关键路径

- **本地 Gradle：** `d:/code/Gradle/gradle-8.9-bin.zip` + `d:/code/Gradle/gradle-8.9/`（已解压）
- **Android SDK：** `D:/Android/SDK`（API 35 已安装）
- **APK 输出：** `app/build/outputs/apk/debug/app-debug.apk`

## App 功能

拍照识鱼（CameraX + ML Kit）、GPS 定位、鱼种数据库（50种×59市）、天气（Open-Meteo）、
钓点收藏（Google Maps）、捕获记录、中英双语、5-Tab Compose UI（MVVM + Room）。
