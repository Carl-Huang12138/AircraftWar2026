# AircraftWar2026 (飞机大战 Android 移动端)

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.google.cn/)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com/)
[![MinSDK](https://img.shields.io/badge/Min%20SDK-API%2030-blue.svg)](https://developer.android.google.cn/studio/releases/platforms)
[![IDE](https://img.shields.io/badge/IDE-Android%20Studio%20Panda-purple.svg)](https://developer.android.google.cn/studio)

本项目为**哈尔滨工业大学（深圳）《软件构造实践》课程实验项目**。旨在将原本基于 Java AWT/Swing 开发的桌面端“飞机大战”游戏，重构并无缝迁移至 **Android 移动平台**。项目深度结合面向对象设计模式，并在保留原有核心玩法的基础上，适配移动端触屏交互、SurfaceView 渲染管线与动态分辨率自适应机制。

---

## 🎮 核心特性与创新机制

1. **SurfaceView 高效渲染管线**：
   * 采用 `SurfaceView` 与独立绘制子线程，结合双缓冲 `Canvas` 渲染机制，实现高帧率流畅游戏画面，彻底规避 Android 主 UI 线程阻塞。
2. **多机型屏幕分辨率自适应**：
   * 在 `surfaceChanged` 生命周期中动态读取设备屏幕实际宽高，游戏背景双图自动平铺无缝滚动，游戏逻辑坐标与碰撞判定按真实分辨率自动缩放。
3. **触屏手势平滑操控**：
   * 取代传统的桌面鼠标监听，重写 `onTouchEvent` 实现手指触点精确追踪，内置战机四周屏幕边缘防出界限制。
4. **特色连击 (Combo) 与动态得分倍率**：
   * 创新引入连击机制：2 秒内连续击毁敌机累加 Combo 数，动态渲染醒目的橘黄色连击特效文字，并提供最高 5 倍的得分加成。
5. **道具与全屏响应机制**：
   * 击毁敌机随机掉落加血、炸弹、火力升级、冰冻等道具；基于观察者模式实现全屏敌机与子弹响应。

---

## 🏗️ 架构与设计模式落地

项目严格遵循面向对象设计原则与经典设计模式：

* **模板方法模式 (Template Method Pattern)**：  
  `BaseGame` 封装游戏主循环的算法骨架（更新、射击、碰撞、绘制、后处理），子类（如 `EasyGame`）仅需重写难度系数、Boss 机制与敌机生成策略。
* **单例模式 (Singleton Pattern)**：  
  `HeroAircraft` 采用双重检查锁定 (DCL) 实现严格单例，并支持安全重置，确保跨对局生命周期稳定。
* **工厂方法模式 (Factory Method Pattern)**：  
  `EnemyFactory`（包含 `MobFactory`、`EliteFactory`、`BossFactory` 等）实现敌机对象的分层解耦创建。
* **策略模式 (Strategy Pattern)**：  
  `ShootStrategy` 将射击弹道（直射、散射、环射）与飞机实体解耦，支持拾取道具后动态切换弹道。
* **观察者模式 (Observer Pattern)**：  
  `PropObserver` 接口实现炸弹与冰冻道具对全屏敌机和敌方子弹的广播通知。

---

## 📂 工程目录结构

```text
AircraftWar2026/
├── app/
│   ├── src/main/
│   │   ├── java/edu/hitsz/
│   │   │   ├── aircraft/        # 战机实体 (HeroAircraft, MobEnemy, EliteEnemy, etc.)
│   │   │   ├── application/     # 游戏核心 (BaseGame, EasyGame, ImageManager, Main)
│   │   │   ├── basic/           # 飞行物基类 (AbstractFlyingObject)
│   │   │   ├── bullet/          # 子弹实体 (HeroBullet, EnemyBullet)
│   │   │   ├── factory/         # 敌机工厂 (MobFactory, EliteFactory, etc.)
│   │   │   ├── prop/            # 道具与观察者 (BloodProp, BombProp, etc.)
│   │   │   ├── strategy/        # 射击策略 (DirectShootStrategy, etc.)
│   │   │   └── MainActivity.java# 应用主入口 Activity
│   │   ├── res/
│   │   │   └── drawable/        # 规范化小写命名的全部游戏图片素材
│   │   └── AndroidManifest.xml  # 应用清单配置
│   └── build.gradle             # 模块构建脚本
├── docs/
│   └── AI_LOG.md                # 课程要求之《AI 辅助开发日志》
├── gradle/                      # Gradle Wrapper 运行时配置
├── build.gradle                 # 项目全局构建脚本
├── settings.gradle              # 项目模块设置
└── README.md                    # 项目说明文档
```

---

## 🚀 快速开始与本地运行指南

方便团队成员或助教直接 Clone 到本地一键运行：

### 1. 环境依赖
* **开发工具**：Android Studio（推荐 Panda / Ladybug / Meerkat 及以上版本）
* **JDK 版本**：Java 17 或 Java 21
* **Android SDK**：API Level 30（Android 11.0）及以上
* **调试设备**：支持 Android 11.0+ 的安卓真机（开启 USB 调试）或 AVD 模拟器

### 2. 克隆项目
打开终端并执行：
```bash
git clone https://github.com/Carl-Huang12138/AircraftWar2026.git
cd AircraftWar2026
```

### 3. 构建与运行
1. 打开 **Android Studio**，选择 **File -> Open...**，定位并选中 `AircraftWar2026` 目录。
2. 等待 Gradle 自动完成依赖同步（若国内环境下载依赖较慢，可使用预设的镜像源）。
3. 连接安卓手机（或启动 AVD 模拟器）。
4. 点击顶部绿色运行按钮 **▶ (Run 'app')**，即可在设备上启动游戏。

---

## 📝 实验日志与过程记录

* 实验过程完整的技术决策与 AI 协作记录见文档：👉 [docs/AI_LOG.md](docs/AI_LOG.md)

---

## 👨‍💻 开发者信息

* **学校**：哈尔滨工业大学（深圳）
* **课程**：软件构造实践 (2026秋)
* **作者**：黄誉铭
