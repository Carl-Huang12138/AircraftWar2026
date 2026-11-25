# 实验四接力开发说明

实验三已由 Xu-Jiyun-xuan 人工审查验收。当前分支包含菜单与三档难度、音频生命周期、结算重开、背景接缝修正及最终射击参数；完成PR合并后，请从远端默认分支 `main` 开始实验四。实验四的新增需求以课程任务书为准，本次没有实现排行榜、持久化或网络对战。

## 获取代码

首次下载：

```bash
git clone https://github.com/Carl-Huang12138/AircraftWar2026.git
cd AircraftWar2026
git switch main
```

已有本地仓库时，先保存自己的未提交修改，再执行：

```bash
git fetch origin
git switch main
git pull --ff-only origin main
```

请在GitHub确认实验三PR已显示Merged，避免从旧的实验二快照或旧APK开始。后续实验请另建分支。

## 环境与构建

- 使用Java 21和Android SDK 36.1，运行设备最低Android 11 / API 30。
- 保留仓库的AGP 9.2.1 / Gradle 9.4.1；当前构建通过，不需要为了版本提示升级依赖。
- Android Studio打开仓库根目录，并在本机配置SDK。`local.properties`、本机IDE改动、构建输出和APK不属于源码提交。
- 建议使用英文项目路径，避免Windows构建工具处理中文路径的问题。

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug

$adbExe = Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'
& $adbExe install -r app\build\outputs\apk\debug\app-debug.apk
& $adbExe install -r app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
& $adbExe shell am instrument -w -r edu.hitsz.test/androidx.test.runner.AndroidJUnitRunner
```

本机离线缓存没有Gradle UTP宿主，因此设备测试使用上面的ADB入口；其他环境可使用正常的 `connectedDebugAndroidTest`。务必检查最终 `OK (23 tests)`，不能只看ADB退出码。单元测试集包含9项项目测试，模板示例测试已移除。

## 代码阅读入口

1. `MenuActivity` 收集难度和音效开关，通过Intent传给 `MainActivity`。
2. `MainActivity` 管理游戏视图、主线程Handler、音频生命周期及结算；重开仅替换本局游戏并复用音频资源。
3. `BaseGame` 用模板方法组织更新、射击、碰撞和绘制；`EasyGame` / `MediumGame` / `HardGame` 提供难度差异。游戏线程只发送 `GameEvent`，不直接弹窗。
4. `CombatTuning` 记录普通/三连发弹速、发射周期和困难Boss参数。环射单独使用 `ringShootCycle`。
5. `ScrollingBackground` 缓存按屏幕缩放的位图，使用MIRROR纵向重复；相位周期为两倍图高。
6. `GameAudioManager` 负责MediaPlayer和SoundPool，后台暂停、销毁释放；不要让工作线程直接调用它。
7. 火力道具委托 `HeroAircraft.activatePowerUp()`。同一英雄的更新与到期回调由同步方法和版本号保护，旧回调不能覆盖新道具，也不能修改下一局英雄；持续时间仍为5秒真实时间。

## 已验收参数

| 参数 | 当前值 |
| --- | --- |
| 普通/三连发周期 | 240ms |
| 普通/三连发标称弹速 | 24像素/逻辑帧 |
| 敌方标称弹速 | 12像素/逻辑帧 |
| 普通Boss | 500血，800ms一轮、20发 |
| 困难Boss | 首只650血，后续每次加150；固定760ms一轮、20发 |
| 环射 | 原始标称弹速8，保留各难度原发射周期 |

斜向弹道使用整数速度分量，存在取整误差。不要把子弹自身速度与敌机移动速度再次叠加。

## 代码审查范围与后续注意

本次清理删除未使用字段/导入、重复射击方法与无意义的模板测试，统一Java异常换行，增加 `.editorconfig` / `.gitattributes`，并集中火力道具计时逻辑。没有修改已验收的难度、道具持续时间和弹幕参数。

实验二遗留的冰冻效果仍使用各实体自己的计时线程；连续重复冰冻时，部分敌机/子弹可能记录已经冻结的速度，影响后续恢复。这不属于本轮火力道具整理，不应误认为已经通过叠加冰冻回归。若后续要改动状态效果，建议先补“重复拾取后恢复原速度”的测试，再统一管理状态。

Lint保留依赖更新、固定竖屏、素材位置等提示。背景和战机素材的位置会影响Android资源密度缩放，移动到 `drawable-nodpi` 前应核对尺寸与碰撞范围，避免借清理提示改变已验收手感。
