# 《软件构造实践》AI 辅助开发日志 (AI Assistance Log)

> **课程名称**：软件构造实践 (2026秋)  
> **项目名称**：飞机大战 (AircraftWar2026) Android 平台迁移与扩展  
> **项目作者**：黄誉铭  
> **日志说明**：本文档用于系统记录课程各个实验阶段求助 AI 的真实过程，遵循 **“遇到什么问题 - AI建议 - 进行的修改”** 结构，聚焦于核心业务逻辑重构与 Android 技术适配。

---

## 目录
- [实验二：代码迁移（从 Windows 到 Android：让飞机大战跑起来）](#实验二代码迁移从-windows-到-android让飞机大战跑起来)
- [实验三：界面交互与音效系统（待更新）](#实验三界面交互与音效系统待更新)
- [实验四：本地存储与排行榜系统（待更新）](#实验四本地存储与排行榜系统待更新)
- [实验五：网络对战与云端同步（待更新）](#实验五网络对战与云端同步待更新)
- [实验六：创新与扩展功能（待更新）](#实验六创新与扩展功能待更新)
- [实验七：结题验收与总结（待更新）](#实验七结题验收与总结待更新)

---

## 实验二：代码迁移（从 Windows 到 Android：让飞机大战跑起来）

### 问题 2.1：桌面端图片资源迁移到 Android（规避 AAPT 资源大小写报错与 OOM 隐患）
* **遇到什么问题**：  
  原 Java Swing 项目中的图片资源存在驼峰命名（如 `elitePlus.png`、`prop_bulletPlus.png`），且原 `ImageManager` 使用 `java.awt.image.BufferedImage` 和 `ImageIO.read()` 加载。直接复制进 Android 会报非法资源命名错误，且桌面端图像 API 无法在 Android 运行。
* **AI建议**：  
  ① Android `res/drawable` 资源命名必须严格为纯小写字母、数字和下划线，需全部重命名为小写 snake_case（如 `elite_plus.png`）；  
  ② 必须改用 Android 原生的 `android.graphics.Bitmap` 和 `BitmapFactory.decodeResource()`；  
  ③ 必须在初始化阶段一次性 decode 缓存至静态 Map，严禁在绘制循环中反复加载，防止发生内存溢出（OOM）。
* **进行的修改**：  
  将 19 张图片规范化小写重命名后导入 `app/src/main/res/drawable/`；将 `AbstractFlyingObject` 中的图像类型改为 `Bitmap`；重构 `ImageManager.java`，编写 `initImage(Context context)` 配合 HashMap 实现安全的预加载与全局静态缓存。

### 问题 2.2：游戏主循环迁移与屏幕分辨率自适应（SurfaceView 双缓冲机制）
* **遇到什么问题**：  
  原项目基于 Swing 的 `JPanel.paint(Graphics g)` 和 `java.util.Timer`，需要迁移为 Android 的 `SurfaceView`，并解决不同手机屏幕分辨率不一致导致的画面偏位与黑屏问题。
* **AI建议**：  
  ① 继承 `SurfaceView` 并实现 `SurfaceHolder.Callback` 与 `Runnable`，在子线程中使用 `lockCanvas` → 绘制 → `unlockCanvasAndPost` 实现 60fps 双缓冲渲染；  
  ② 在 `surfaceChanged` 中读取手机真实屏幕宽度与高度，动态更新 `Main.WINDOW_WIDTH` 与 `WINDOW_HEIGHT`，使英雄机与背景按真实分辨率等比适配；  
  ③ 在 `surfaceDestroyed` 中妥善结束绘制子线程，防止 Activity 销毁后后台仍在刷屏造成内存泄漏。
* **进行的修改**：  
  新建 `BaseGame.java`（SurfaceView 循环基类）与 `EasyGame.java`；实现背景双图滚动拼缝算法；在 `MainActivity.java` 中直接设置 `setContentView(new EasyGame(this))`，成功在真机/模拟器上渲染出平滑滚动的星空背景与居中的英雄机。

### 问题 2.3：触屏交互适配与游戏逻辑闭环（连击 Combo 机制还原）
* **遇到什么问题**：  
  原项目使用鼠标拖动控制战机，需改为手指触屏拖拽；同时需要将子弹射击、敌机批量生成、碰撞判定、道具掉落及连续击杀 Combo 机制接入新的主循环。
* **AI建议**：  
  ① 重写 `BaseGame` 的 `onTouchEvent`，监听 `ACTION_DOWN` 和 `ACTION_MOVE`，并对战机位置添加 `[width/2, screenWidth - width/2]` 严格边界防出界限制；  
  ② 在游戏主循环 `action()` 中顺序组织：连击超时检测、敌机生成周期、敌我射击周期、子弹/敌机/道具移动、碰撞检测（`crashCheckAction`）与失效对象清理（`removeIf`）；  
  ③ 还原原项目中 2 秒内连续击杀判定机制，在 Canvas 上动态渲染橘黄色 `COMBO xN !!` 连击特效字体及最高 5 倍的得分计算。
* **进行的修改**：  
  在 `BaseGame.java` 中完整实现了触屏控制与所有碰撞、掉落、计分逻辑；当英雄机生命归零时在屏幕中央渲染 `GAME OVER` 提示，完成了实验二要求的“画得出来、动得起来、碰得准确”全部闭环。

---

## 实验三：界面交互与音效系统（待更新）

### 问题 3.1：[待补充]
* **遇到什么问题**：  
* **AI建议**：  
* **进行的修改**：  

---

## 实验四：本地存储与排行榜系统（待更新）

### 问题 4.1：[待补充]
* **遇到什么问题**：  
* **AI建议**：  
* **进行的修改**：  

---

## 实验五：网络对战与云端同步（待更新）

### 问题 5.1：[待补充]
* **遇到什么问题**：  
* **AI建议**：  
* **进行的修改**：  

---

## 实验六：创新与扩展功能（待更新）

### 问题 6.1：[待补充]
* **遇到什么问题**：  
* **AI建议**：  
* **进行的修改**：  

---

## 实验七：结题验收与总结（待更新）

### 问题 7.1：[待补充]
* **遇到什么问题**：  
* **AI建议**：  
* **进行的修改**：  
