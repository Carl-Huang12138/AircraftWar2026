# 实验三 Implementation Plan

> **For agentic workers:** 使用 executing-plans 在当前会话逐项执行；用户已要求开始实验3，遇到实际阻塞时停止询问。

**Goal:** 在实验二基础上完成菜单、难度切换、音乐开关、原生音频管理与 Handler 结算。

**Architecture:** MenuActivity 以 XML 展示选择并通过 Intent 启动 MainActivity；BaseGame 保留模板方法，补齐 MediumGame/HardGame。游戏线程只发 Message，Activity 在主线程控制独立音频管理类与结算弹窗。

**Tech Stack:** Java、Android Views、SurfaceView、Handler、MediaPlayer、SoundPool、现有 Gradle 与 AndroidX 测试。

**Spec:** `D:/软件构造实践/课件/实验3.pdf`（第3、17–23、28–32页）；`D:/软件构造实践/实验二交接文档.docx`。

## Global Constraints

- 使用用户指定的 `D:/AndroidAircraftWar/AircraftWar2026`，独立开发分支 `feat/exp3-ui-audio`，保留两处已有 IDE 配置改动。
- Java 21 构建；SDK 36.1；minSdk 30；不升级现有依赖。
- 复用原项目六个 WAV，原文件保持不变。
- 游戏后台暂停；Activity 销毁释放音频；不自动 push、合并或提交课程平台。
- 本次不实现实验四排行榜、存储或实验五网络功能。

## Review Focus

- 三种难度都能启动正确的场景；未知参数回退简单。
- 游戏返回菜单后再开始，不遗留游戏线程或上一局英雄状态。
- Home/返回、Surface 重建、快速切换不重复启动循环，不在后台继续运行。
- 音频未加载、静音、Boss切换、游戏结束、重复释放时状态安全。
- 游戏结束只发送一次消息；后台收到消息不弹窗，回前台才呈现。

### Task 1: 菜单与三档难度

**Files:** 新建 MenuActivity、Difficulty、MediumGame、HardGame、activity_menu.xml；修改 MainActivity、Manifest、strings、主题；新增 DifficultyTest、Experiment3Test。

**Interfaces:** `Difficulty.fromExtra(String)` 返回 EASY/MEDIUM/HARD；Intent 使用 `difficulty` 与 `sound_enabled`；MainActivity 依难度创建 BaseGame。

- [x] 写参数回退、菜单控件、三种难度及返回流程测试，先验证因功能缺失而失败。
- [x] 实现菜单与难度，保留星空和战机资源，适配系统栏及小屏滚动。
- [x] 构建并在 Shared_API_36 验证菜单与三种场景。

### Task 2: 生命周期、Handler 与结算

**Files:** 修改 BaseGame、MainActivity；新增 GameEvent；扩展设备测试。

**Interfaces:** `BaseGame.setEventHandler(Handler)`、`resumeGame()`、`pauseGame()`；`GameEvent` 定义 HIT/SUPPLY/BOMB/BOSS/GAME_OVER；GAME_OVER.arg1 为分数。

- [x] 写主线程收到一次结束消息、暂停后游戏时间不变、重开隔离测试并验证失败。
- [x] 实现可停止的单游戏循环、触摸与逻辑同步、一次性结算消息，主线程弹窗“再来一局/返回菜单”。
- [x] 设备测试前后台、退出重入、结束重开及线程回收。

### Task 3: 音频与交付验证

**Files:** 新增 music/GameAudioManager、res/raw 六个资源；修改 BaseGame 音效触发点、MainActivity 音频生命周期；更新 README、docs/AI_LOG.md、docs/EXPERIMENT3.md。

**Interfaces:** `GameAudioManager(Context, boolean)`；`resume()`、`pause()`、`setBossPresent(boolean)`、`playEffect(int)`、`gameOver()`、`release()`，所有调用来自主线程。

- [x] 写静音、播放暂停恢复、Boss互斥切换、结束不恢复和重复释放设备测试并验证失败。
- [x] 实现长音频 MediaPlayer、短音频 SoundPool，加载完成后播放，失败记录日志，释放后不再使用。
- [x] 执行 unit tests、connected device tests、lint、assemble；保存真实截图与验证记录。
- [x] 对最终补丁进行独立审查，修复重要问题；记录测试实际结果及未验证项，交付 APK，不自动推送。

## 执行记录

- 使用用户指定的现有 checkout，在新分支开发，原 IDE 配置改动保留。
- 初始基线构建与原单元测试通过；新需求测试先因缺失接口而失败。
- 4 项单元测试、11 项设备测试及 APK 构建通过；Lint 0 错误并保留警告。
- 独立审查一项重要发现（BGM 暂停位置）：回归测试先失败，修复后全套通过。无其他重要或次要待办。
- 原桌面音频、难度参数复用；改正敌机初始血量上限和与结束事件相关的碰撞逻辑，附回归覆盖。
- 未推送、合并、提交课程平台；实际试听与手感由成员完成。
