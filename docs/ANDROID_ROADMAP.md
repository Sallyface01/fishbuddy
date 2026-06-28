# FishBuddy Android — 开发路线图

## 总览

| Phase | 内容 | 状态 | 关键产出 |
|-------|------|------|----------|
| Phase 0 | 项目脚手架 | ✅ | Gradle / Manifest / Theme / 5 Tab 导航 |
| Phase 1 | Room 数据层 | ⬜ | 实体 + DAO + Database + JSON 加载 |
| Phase 2 | 分析流水线 | ⬜ | CameraX + 定位 + ML Kit + 数据库查询 |
| Phase 3 | 钓点收藏 | ⬜ | Google Maps + 标记 + 复制坐标 |
| Phase 4 | 鱼种图鉴 | ⬜ | 搜索/筛选/网格 + 详情 + 捕获记录 |
| Phase 5 | 天气集成 | ⬜ | Open-Meteo API（同 iOS） |
| Phase 6 | 国际化完善 | ⬜ | values-zh + values-en 全覆盖 |
| Phase 7 | 打磨测试 | ⬜ | Bug 修复 + UI 一致性 |

---

## 技术栈对照

| 功能 | iOS | Android |
|------|-----|---------|
| UI | SwiftUI | Jetpack Compose + Material 3 |
| 数据库 | SwiftData | Room |
| 图片分类 | Vision | ML Kit Image Labeling |
| 定位 | CoreLocation | Fused Location Provider |
| 相机 | PhotosUI / AVFoundation | CameraX |
| 地图 | MapKit | Google Maps Compose |
| 天气 | Open-Meteo | Open-Meteo（同 API） |
| JSON | Codable | Gson |
| 网络 | URLSession | OkHttp |
| 语言 | String Catalog | values/strings.xml |

## 可直接复用的资源

| 资源 | 来源 | 操作 |
|------|------|------|
| fish_database.json | iOS Resources/Data | ✅ 已拷贝 |
| fishing_methods.json | iOS Resources/Data | ✅ 已拷贝 |
| 设计规范 | iOS DESIGN_SPEC.md | 同配色/组件 |
| 产品需求 | iOS REQUIREMENTS.md | 同功能定义 |

---

## Phase 0: 项目脚手架

**目标**: Android Studio 一键打开即编译，5 Tab 空壳导航。

**任务清单**:
- [x] P0-1: Gradle 构建文件（project + app）
- [x] P0-2: AndroidManifest.xml（权限 + Activity）
- [x] P0-3: Application + MainActivity
- [x] P0-4: Material 3 主题（appBlue #0066CC）
- [x] P0-5: 5 Tab 导航壳（分析/钓点/图鉴/记录/设置）
- [x] P0-6: values/strings.xml + values-en/strings.xml
- [x] P0-7: Launcher icon（adaptive icon XML）
- [x] P0-8: Bug 修复（material-icons-extended / AppDatabase 引用 / shadow API）

**验收**: Android Studio 打开即编译，5 个 Tab 可切换，白底蓝字主题。

---

## Phase 1: Room 数据层

**目标**: 数据库就绪，JSON 鱼种数据可查询。

**任务清单**:
- [ ] P1-1: Room 实体（AnalysisRecord / FishingSpot / CatchLog）
- [ ] P1-2: DAO 接口（CRUD for each entity）
- [ ] P1-3: AppDatabase（Room Database 单例）
- [ ] P1-4: FishDatabaseService（加载 JSON → 城市匹配）
- [ ] P1-5: FishingMethodsService（加载钓法 JSON）
- [ ] P1-6: 单元测试

**验收**: 可根据水域类型 + GPS 坐标查询到鱼种列表。

---

## Phase 2: 分析流水线

**目标**: 拍照 → 定位 → 分类 → 查询 → 展示（完整打通）。

**任务清单**:
- [ ] P2-1: CameraService（CameraX 拍照）
- [ ] P2-2: LocationService（FusedLocationProvider）
- [ ] P2-3: WaterClassifierService（ML Kit Image Labeling）
- [ ] P2-4: HomeViewModel（编排完整流程）
- [ ] P2-5: HomeScreen（大蓝色按钮 + Loading + 错误处理）
- [ ] P2-6: AnalysisResultScreen（水域概况/鱼种/钓法）

**验收**: 拍照后 3 秒内出分析结果，离线可用（除天气）。

---

## Phase 3: 钓点收藏

**目标**: 地图标记 + 列表 + 复制坐标 + 导航。

**任务清单**:
- [ ] P3-1: SpotsViewModel
- [ ] P3-2: SpotsScreen（Google Maps + 底部列表）
- [ ] P3-3: AddSpotSheet + SpotDetailSheet
- [ ] P3-4: 复制坐标到剪贴板 + 打开地图 App

**验收**: 地图显示标记，列表可浏览，可复制坐标导航。

---

## Phase 4: 鱼种图鉴

**目标**: 50 种鱼浏览 + 筛选 + 详情 + 捕获记录。

**任务清单**:
- [ ] P4-1: GuideViewModel（搜索/水域/区域筛选）
- [ ] P4-2: GuideScreen（搜索框 + chips + 网格）
- [ ] P4-3: SpeciesDetailScreen（简介/钓法/饵料/季节）
- [ ] P4-4: CatchLog 表单（照片/心得/日期/位置）
- [ ] P4-5: 捕获记录列表（按鱼种归类）

**验收**: 图鉴可浏览 50 种鱼，可按水域/区域筛选，可记录捕获。

---

## Phase 5: 天气集成

**目标**: 获取 Open-Meteo 天气数据，融入分析结果。

**任务清单**:
- [ ] P5-1: WeatherData 数据类
- [ ] P5-2: WeatherService（OkHttp + Open-Meteo API）
- [ ] P5-3: WeatherBadge Compose 组件
- [ ] P5-4: 集成到 AnalysisResultScreen

**验收**: 分析结果显示天气卡片 + 钓鱼气象判断。

---

## Phase 6: 国际化完善

**目标**: 所有界面字符串中英双语覆盖。

**任务清单**:
- [ ] P6-1: 补充 values/strings.xml（全量中文）
- [ ] P6-2: 补充 values-en/strings.xml（全量英文）
- [ ] P6-3: Compose 中使用 stringResource()

**验收**: 系统语言切英文后所有界面显示英文。

---

## Phase 7: 打磨测试

**目标**: Bug 修复 + 边角情况 + UI 一致性。

**任务清单**:
- [ ] P7-1: Repository 单元测试
- [ ] P7-2: ViewModel 单元测试
- [ ] P7-3: UI 边角情况（空状态/加载失败/权限拒绝）
- [ ] P7-4: 与 iOS 端 UI 一致性核对

**验收**: 测试覆盖率 > 70%，两平台 UI 一致。

---

## 当前状态摘要

- **工作目录**: `d:\code\Swift\android-fishbuddy\`
- **iOS 参照**: `d:\code\Swift\swift-executable\`
- **JSON 数据**: 已复用（fish_database.json + fishing_methods.json）
- **下一步**: Phase 0 — 项目脚手架
