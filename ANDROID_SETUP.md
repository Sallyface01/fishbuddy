# FishBuddy Android — 搭建与运行指南

## 环境要求

| 工具 | 说明 |
|------|------|
| **Android Studio** | Hedgehog (2024.1) 或更新版本 |
| **JDK** | JDK 17（Android Studio 自带） |
| **Android SDK** | API 35 + Build Tools（Android Studio 自带） |
| **设备** | Android 8.0+ 真机 或 Android 模拟器 |

## 第一步：打开项目

```
1. 启动 Android Studio
2. File → Open
3. 选择 android-fishbuddy 目录（包含 settings.gradle.kts 的那个）
4. 等待 Gradle Sync 完成（首次约 2-5 分钟）
```

## 第二步：配置 Google Maps API Key

钓点地图功能需要 Google Maps。

```
1. 打开 app/src/main/AndroidManifest.xml
2. 找到 <meta-data android:name="com.google.android.geo.API_KEY" android:value="YOUR_MAPS_API_KEY" />
3. 把 YOUR_MAPS_API_KEY 替换为你的密钥
4. 获取密钥：https://console.cloud.google.com → APIs → Maps SDK for Android
```

> 如果暂时不需要地图功能，可以跳过这一步。地图会显示空白但不影响其他功能。

## 第三步：运行

```
方式一：真机
  1. 手机开启「开发者选项」→「USB 调试」
  2. USB 连接电脑
  3. 点击 ▶ Run

方式二：模拟器
  1. Tools → Device Manager → Create Device
  2. 选择 Pixel 8 + API 35 镜像
  3. 启动模拟器 → 点击 ▶ Run
```

## 项目结构

```
android-fishbuddy/
├── ANDROID_SETUP.md              ← 本文件
├── settings.gradle.kts
├── build.gradle.kts
├── app/
│   ├── build.gradle.kts           ← 依赖配置
│   └── src/main/
│       ├── AndroidManifest.xml    ← 权限 + Activity
│       ├── assets/data/           ← JSON 鱼种数据库
│       ├── res/
│       │   ├── values/            ← 中文 strings
│       │   └── values-en/         ← 英文 strings
│       └── java/com/fishbuddy/app/
│           ├── MainActivity.kt
│           ├── data/              ← Room 数据库
│           ├── domain/model/      ← 数据类
│           ├── service/           ← 服务层
│           └── ui/                ← Compose UI
└── docs/                          ← 技术文档
```

## 常见问题

| 问题 | 解决 |
|------|------|
| Gradle Sync 失败 | File → Invalidate Caches → Restart |
| "SDK not found" | Tools → SDK Manager → 安装 API 35 |
| 相机黑屏 | 检查 AndroidManifest 相机权限 |
| 定位失败 | 手机设置 → 应用 → FishBuddy → 权限 → 位置 |
| 地图不显示 | 检查 Google Maps API Key 是否正确 |
| 中文不显示 | 检查设备系统语言设置 |

## 功能对照 iOS

| 功能 | 状态 |
|------|------|
| 📷 拍照分析 | ✅ CameraX + ML Kit |
| 📍 GPS 定位 | ✅ FusedLocation |
| 🌊 水域分类 | ✅ ML Kit |
| 🐟 鱼种数据库 | ✅ 50 种 × 59 市 |
| ☁️ 天气 | ✅ Open-Meteo |
| 🗺️ 钓点收藏 | ✅ Google Maps |
| 📖 鱼种图鉴 | ✅ 搜索 + 筛选 |
| 📝 捕获记录 | ✅ 图鉴集成 |
| 🌐 中英双语 | ✅ 93 条 × 2 |
