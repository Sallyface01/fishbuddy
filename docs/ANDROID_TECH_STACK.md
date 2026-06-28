# Android 技术栈规范

## 对照 iOS

| 层 | iOS | Android |
|----|-----|---------|
| UI | SwiftUI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Service | MVVM + Repository |
| 状态 | @Observable | StateFlow / ViewModel |
| 数据库 | SwiftData @Model | Room Entity + DAO |
| DI | 手动单例 | 手动单例（同 iOS 无依赖） |
| 导航 | TabView + NavigationStack | NavigationBar + NavHost |
| 图片 | PhotosUI / AVFoundation | CameraX |
| 分类 | Vision | ML Kit Image Labeling |
| 定位 | CoreLocation | FusedLocationProvider |
| 地图 | MapKit | Google Maps Compose |
| JSON | Codable | Gson |
| 网络 | URLSession | OkHttp |
| i18n | String Catalog | strings.xml (values/values-en) |

## 代码规范

### 文件组织
```
com.fishbuddy.app/
├── FishBuddyApp.kt              ← Application
├── MainActivity.kt              ← 入口
├── data/
│   ├── model/                   ← Room Entity
│   ├── local/                   ← DAO + Database
│   └── repository/              ← Repository
├── domain/model/                ← 纯数据类
├── service/                     ← 外部服务
├── ui/
│   ├── navigation/              ← NavHost
│   ├── theme/                   ← Theme + Colors
│   ├── home/                    ← Screen + ViewModel
│   ├── guide/                   ← Screen + ViewModel
│   ├── spots/                   ← Screen + ViewModel
│   ├── history/                 ← Screen + ViewModel
│   ├── settings/                ← Screen + ViewModel
│   └── components/              ← 共享组件
└── util/                        ← 工具类
```

### 命名规范
- Activity: `MainActivity`
- Screen: `HomeScreen`, `GuideScreen`
- ViewModel: `HomeViewModel`, `GuideViewModel`
- Entity: `AnalysisRecordEntity`, `FishingSpotEntity`
- Repository: `AnalysisRepository`, `SpotRepository`

### 禁止事项
- 无第三方依赖（仅 Android 官方库）
- 禁止在 Composable 中直接操作数据库
- 禁止硬编码中文（统一用 stringResource）
- Google Maps API Key 不放代码中（用 local.properties）

## 最低版本

- minSdk: 26 (Android 8.0)
- targetSdk / compileSdk: 35 (Android 15)
- Kotlin: 2.1.0
- Compose BOM: 2024.12
