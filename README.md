# AircraftWar2026 (飞机大战 Android 移动端)

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.google.cn/)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com/)
[![MinSDK](https://img.shields.io/badge/Min%20SDK-API%2030-blue.svg)](https://developer.android.google.cn/studio/releases/platforms)
[![IDE](https://img.shields.io/badge/IDE-Android%20Studio%20Panda-purple.svg)](https://developer.android.google.cn/studio)

本项目为**哈尔滨工业大学（深圳）《软件构造实践》课程实验项目**。旨在将原本基于 Java AWT/Swing 开发的桌面端“飞机大战”游戏，重构并无缝迁移至 **Android 移动平台**。项目深度结合面向对象设计模式，并在保留原有核心玩法的基础上，适配移动端触屏交互、SurfaceView 渲染管线与动态分辨率自适应机制。

---

## 实验三：菜单、难度与音效

应用启动后进入主菜单，可选择简单/普通/困难与音乐音效开关。游戏支持返回菜单，结束后通过主线程 Handler 显示得分并重开。背景音乐和 Boss 音乐使用 MediaPlayer，短音效使用 SoundPool；进入后台暂停，退出释放资源。

运行、代码阅读顺序及真实验证记录见 [docs/EXPERIMENT3.md](docs/EXPERIMENT3.md)。普通/三连发均为240ms、标称弹速24，敌弹12，环射保持原参数；困难Boss首只650血、后续每次加150，固定760ms一轮，仍高于普通Boss的500血、800ms一轮。保留背景镜像连续滚动。用户已完成人工验收；代码审查清理后，构建、9项单元测试和23项模拟器测试通过，Lint为0错误、37条警告。

## 🎮 核心特性与创新机制

1. **SurfaceView 高效渲染管线**：
   * `SurfaceView` 子线程负责游戏更新和绘制，按40ms帧预算运行；菜单、音频事件和结算交互由主线程处理。
2. **多机型屏幕分辨率自适应**：
   * 在 `surfaceChanged` 中读取实际画布宽高，背景按尺寸缓存缩放并通过镜像纹理连续滚动，战机移动边界使用当前画布尺寸。
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
  `HeroAircraft` 采用双重检查锁定 (DCL)，每局开始时重置，供当前场景和道具共享访问；火力计时器持有本局英雄引用。
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
│   │   │   ├── application/     # 游戏循环、三档难度、背景与参数
│   │   │   ├── basic/           # 飞行物基类 (AbstractFlyingObject)
│   │   │   ├── bullet/          # 子弹实体 (HeroBullet, EnemyBullet)
│   │   │   ├── factory/         # 敌机工厂 (MobFactory, EliteFactory, etc.)
│   │   │   ├── prop/            # 道具与观察者 (BloodProp, BombProp, etc.)
│   │   │   ├── strategy/        # 射击策略 (DirectShootStrategy, etc.)
│   │   │   ├── music/           # MediaPlayer / SoundPool 生命周期
│   │   │   ├── MenuActivity.java# 启动菜单
│   │   │   └── MainActivity.java# 游戏页面与结算
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
* **JDK 版本**：Java 21（当前构建工具要求）
* **Android SDK**：构建使用 API 36.1；运行设备最低 API 30（Android 11.0）
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
* 实验四接力开发入口、构建和回归命令见：[docs/EXPERIMENT4_HANDOFF.md](docs/EXPERIMENT4_HANDOFF.md)。

---

## 👨‍💻 开发者信息

* **学校**：哈尔滨工业大学（深圳）
* **课程**：软件构造实践 (2026秋)
* **作者**：黄誉铭（实验二迁移）、Xu-Jiyun-xuan（实验三接力，AI 辅助）
