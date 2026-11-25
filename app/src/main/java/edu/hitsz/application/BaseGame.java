package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.AceAircraft;
import edu.hitsz.aircraft.AdvancedAircraft;
import edu.hitsz.aircraft.BossAircraft;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.factory.AceFactory;
import edu.hitsz.factory.AdvancedFactory;
import edu.hitsz.factory.BossFactory;
import edu.hitsz.factory.EliteFactory;
import edu.hitsz.factory.EnemyFactory;
import edu.hitsz.factory.MobFactory;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FreezeProp;

import java.util.LinkedList;
import java.util.List;

/**
 * 游戏主视图基类 (SurfaceView + 子线程游戏循环)
 * 采用模板方法模式 (Template Method Pattern)
 * @author hitsz
 */
public abstract class BaseGame extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    protected String difficulty;
    protected int screenWidth = 512;
    protected int screenHeight = 768;
    protected int backGroundTop = 0;
    private ScrollingBackground background;

    protected volatile boolean mbLoop = false;
    protected final Object gameLock = new Object();
    private boolean surfaceReady;
    private boolean surfaceSized;
    private boolean resumed;
    private volatile Handler eventHandler;
    protected SurfaceHolder mSurfaceHolder;
    protected Paint mPaint;
    protected Thread gameThread;

    protected HeroAircraft heroAircraft;
    protected List<AbstractAircraft> enemyAircraft;
    protected List<BaseBullet> heroBullets;
    protected List<BaseBullet> enemyBullets;
    protected List<AbstractProp> props;

    protected int enemyMaxNumber;
    protected int score = 0;
    protected boolean gameOverFlag = false;

    // 时间与创新功能连击机制
    protected int timeInterval = 40;
    protected int timeCount = 0;
    protected int comboCount = 0;
    protected long lastKillTime = 0;

    // 敌机与射击计时计数器
    protected double enemySpawnCycle = 20;
    protected int enemySpawnCounter = 0;
    protected final double heroShootCycle = CombatTuning.HERO_SHOOT_CYCLE_TICKS;
    protected double ringShootCycle = 15; // 仅环射沿用初版道具周期。
    protected int heroShootCounter = 0;
    protected double enemyShootCycle = 25;
    protected int enemyShootCounter = 0;
    protected int bossShootCounter = 0;

    protected double enemyHpMultiplier = 1.0;
    protected double enemySpeedMultiplier = 1.0;
    protected boolean bossExisting = false;
    protected int scoreThreshold = 1000;

    // 敌机工厂
    protected final EnemyFactory mobFactory = new MobFactory();
    protected final EnemyFactory eliteFactory = new EliteFactory();
    protected final EnemyFactory advancedFactory = new AdvancedFactory();
    protected final EnemyFactory aceFactory = new AceFactory();
    protected final EnemyFactory bossFactory = new BossFactory();

    public BaseGame(Context context) {
        super(context);
        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.setFocusable(true);

        // 初始化图片静态资源
        ImageManager.initImage(context);

        this.difficulty = getDifficultyName();
        this.enemyMaxNumber = getEnemyMaxNumber();

        // 重置并获取单例英雄机
        HeroAircraft.resetInstance();
        this.heroAircraft = HeroAircraft.getInstance(
                screenWidth / 2,
                screenHeight - 200,
                0, 0, getHeroHp());

        this.enemyAircraft = new LinkedList<>();
        this.heroBullets = new LinkedList<>();
        this.enemyBullets = new LinkedList<>();
        this.props = new LinkedList<>();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        surfaceReady = true;
        surfaceSized = false;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        synchronized (gameLock) {
            this.screenWidth = width;
            this.screenHeight = height;
            Bitmap source = ImageManager.getBackgroundImage(difficulty);
            if (source != null && (background == null || !background.hasSize(width, height))) {
                // 仅在尺寸变化时缩放，避免软件画布每帧重复缩放整张背景。
                background = new ScrollingBackground(source, width, height);
            }
            Main.WINDOW_WIDTH = width;
            Main.WINDOW_HEIGHT = height;
            if (heroAircraft != null) {
                heroAircraft.setLocation(screenWidth / 2.0, screenHeight - 250);
            }
            surfaceSized = true;
        }
        startGameLoop();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        surfaceReady = false;
        surfaceSized = false;
        stopGameLoop();
    }

    public void setEventHandler(Handler handler) { eventHandler = handler; }

    protected void sendGameEvent(int event, int value) {
        Handler handler = eventHandler;
        if (handler != null) handler.obtainMessage(event, value, 0).sendToTarget();
    }

    public void resumeGame() {
        resumed = true;
        startGameLoop();
    }

    public void pauseGame() {
        resumed = false;
        stopGameLoop();
    }

    private void startGameLoop() {
        synchronized (gameLock) {
            if (!surfaceReady || !surfaceSized || !resumed || gameThread != null) return;
            mbLoop = true;
            gameThread = new Thread(this, "AircraftWar-GameLoop");
            gameThread.start();
        }
    }

    private void stopGameLoop() {
        // 与一次逻辑更新互斥，退出返回前确保上一局不再修改模型。
        // 不在 UI 线程 join 持有 Surface 的线程，避免生命周期死锁。
        synchronized (gameLock) {
            mbLoop = false;
            Thread oldThread = gameThread;
            gameThread = null;
            if (oldThread != null) oldThread.interrupt();
        }
    }

    public boolean hasBoss() {
        synchronized (gameLock) { return bossExisting; }
    }

    public boolean isGameOver() {
        synchronized (gameLock) { return gameOverFlag; }
    }

    public int getScore() {
        synchronized (gameLock) { return score; }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        synchronized (gameLock) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                if (heroAircraft != null && !gameOverFlag) {
                    float x = event.getX();
                    float y = event.getY();
                    // 限制在屏幕可见范围内
                    x = Math.max(heroAircraft.getWidth() / 2f, Math.min(x, screenWidth - heroAircraft.getWidth() / 2f));
                    y = Math.max(heroAircraft.getHeight() / 2f, Math.min(y, screenHeight - heroAircraft.getHeight() / 2f));
                    heroAircraft.setLocation(x, y);
                }
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) performClick();
        return true;
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public void run() {
        Thread current = Thread.currentThread();
        while (mbLoop) {
            long frameStart = System.nanoTime();
            synchronized (gameLock) {
                // 旧线程被停止后即使晚醒，也不能加入新一轮循环。
                if (!mbLoop || current != gameThread) return;
                if (!gameOverFlag) {
                    action();
                }
                draw();
                if (gameOverFlag) {
                    mbLoop = false;
                    gameThread = null;
                    return;
                }
            }
            try {
                long remaining = timeInterval * 1_000_000L - (System.nanoTime() - frameStart);
                if (remaining > 0) {
                    Thread.sleep(remaining / 1_000_000L, (int) (remaining % 1_000_000L));
                } else {
                    // 超预算时也让出线程，避免连续抢占 gameLock 阻塞前后台切换。
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * 游戏主时序数据驱动逻辑
     */
    protected void action() {
        // 连击超时断连判定 (超过2秒未击杀重置连击)
        if (System.currentTimeMillis() - lastKillTime > 2000) {
            comboCount = 0;
        }

        timeCount += timeInterval;
        updateDifficulty();

        // 敌机生成
        enemySpawnCounter++;
        if (enemySpawnCounter >= enemySpawnCycle) {
            enemySpawnCounter = 0;
            spawnEnemyAction();
        }

        shootAction();
        bulletsMoveAction();
        aircraftMoveAction();
        propsMoveAction();
        crashCheckAction();
        postProcessAction();
        checkResultAction();
    }

    private void spawnEnemyAction() {
        if (score >= scoreThreshold && !bossExisting) {
            spawnBoss();
        }

        if (enemyAircraft.size() < enemyMaxNumber) {
            AbstractAircraft newEnemy = produceEnemy();
            if (newEnemy != null) {
                newEnemy.scaleInitialHp(enemyHpMultiplier);

                int enhancedSpeedY = (int) (newEnemy.getSpeedY() * enemySpeedMultiplier);
                newEnemy.setSpeedY(enhancedSpeedY);

                enemyAircraft.add(newEnemy);
            }
        }
    }

    private void shootAction() {
        // 英雄机自动射击
        heroShootCounter++;
        double currentHeroShootCycle = heroAircraft != null && heroAircraft.isRingShooting()
                ? ringShootCycle : heroShootCycle;
        if (heroShootCounter >= currentHeroShootCycle) {
            heroShootCounter = 0;
            if (heroAircraft != null) {
                heroBullets.addAll(heroAircraft.shoot());
            }
        }

        // 敌机射击
        enemyShootCounter++;
        if (enemyShootCounter >= enemyShootCycle) {
            enemyShootCounter = 0;
            for (AbstractAircraft enemy : enemyAircraft) {
                if (!(enemy instanceof BossAircraft)) enemyBullets.addAll(enemy.shoot());
            }
        }
        // Boss 独立计时，困难模式的小敌机加速不再挤高 Boss 弹幕密度。
        if (bossExisting) {
            bossShootCounter++;
            if (bossShootCounter >= getBossShootCycle()) {
                bossShootCounter = 0;
                for (AbstractAircraft enemy : enemyAircraft) {
                    if (enemy instanceof BossAircraft) enemyBullets.addAll(enemy.shoot());
                }
            }
        } else {
            bossShootCounter = 0;
        }
    }

    protected double getBossShootCycle() { return enemyShootCycle; }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftMoveAction() {
        for (AbstractAircraft enemy : enemyAircraft) {
            enemy.forward();
        }
    }

    private void propsMoveAction() {
        for (AbstractProp prop : props) {
            prop.forward();
        }
    }

    private void crashCheckAction() {
        // 1. 敌机子弹击中英雄机
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) continue;
            if (heroAircraft != null && heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
                sendGameEvent(GameEvent.HIT, 0);
            }
        }

        // 2. 英雄机子弹击中敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) continue;
            for (AbstractAircraft enemy : enemyAircraft) {
                if (enemy.notValid()) continue;
                if (enemy.crash(bullet)) {
                    enemy.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    sendGameEvent(GameEvent.HIT, 0);

                    if (enemy.notValid()) {
                        // 连击数累加
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastKillTime <= 2000) {
                            comboCount++;
                        } else {
                            comboCount = 1;
                        }
                        lastKillTime = currentTime;

                        int multiplier = Math.min(comboCount, 5);
                        int baseScore = 10;
                        if (enemy instanceof BossAircraft) {
                            baseScore = 100;
                            bossExisting = false;
                            sendGameEvent(GameEvent.BOSS, 0);
                        } else if (enemy instanceof AceAircraft) {
                            baseScore = 50;
                        } else if (enemy instanceof AdvancedAircraft) {
                            baseScore = 30;
                        } else if (enemy instanceof EliteEnemy) {
                            baseScore = 20;
                        }

                        // 连击倍率计分
                        score += baseScore * multiplier;

                        // 掉落道具
                        props.addAll(enemy.dropProps());
                    }
                }

                if (bullet.notValid()) break;
            }
        }

        // 机身碰撞独立于子弹列表：没有英雄子弹时也应能判定。
        for (AbstractAircraft enemy : enemyAircraft) {
            if (!enemy.notValid() && heroAircraft != null && !heroAircraft.notValid()
                    && (enemy.crash(heroAircraft) || heroAircraft.crash(enemy))) {
                enemy.vanish();
                heroAircraft.decreaseHp(Integer.MAX_VALUE);
                sendGameEvent(GameEvent.HIT, 0);
            }
        }

        // 3. 英雄机拾取道具
        for (AbstractProp prop : props) {
            if (prop.notValid()) continue;
            if (heroAircraft != null && heroAircraft.crash(prop)) {
                if (prop instanceof BombProp) {
                    for (AbstractAircraft enemy : enemyAircraft) {
                        if (enemy.notValid() || enemy instanceof BossAircraft) continue;
                        if (enemy instanceof AceAircraft) {
                            if (enemy.getHp() <= 50) score += 50;
                        } else if (enemy instanceof EliteEnemy) {
                            score += 20;
                        } else if (enemy instanceof AdvancedAircraft) {
                            score += 30;
                        } else {
                            score += 10;
                        }
                    }
                }

                if (prop instanceof BombProp || prop instanceof FreezeProp) {
                    for (AbstractAircraft enemy : enemyAircraft) {
                        prop.addObserver(enemy);
                    }
                    for (BaseBullet enemyBullet : enemyBullets) {
                        prop.addObserver(enemyBullet);
                    }
                }
                prop.active();
                prop.vanish();
                sendGameEvent(prop instanceof BombProp ? GameEvent.BOMB : GameEvent.SUPPLY, 0);
            }
        }
    }

    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircraft.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);
    }

    private void checkResultAction() {
        if (!gameOverFlag && heroAircraft != null && heroAircraft.getHp() <= 0) {
            gameOverFlag = true;
            sendGameEvent(GameEvent.GAME_OVER, score);
        }
    }

    /**
     * 游戏画面绘制 (双缓冲 Canvas 渲染)
     */
    public void draw() {
        Canvas canvas;
        try {
            canvas = mSurfaceHolder.lockCanvas();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            Log.w("BaseGame", "Surface is no longer available", exception);
            return;
        }
        if (canvas == null) {
            return;
        }
        try {
            // 不依赖 Surface 缓冲区保留旧内容，每帧完整清空并重绘。
            canvas.drawColor(Color.BLACK);
            // 1. 绘制滚动背景
            if (background != null) {
                background.draw(canvas, backGroundTop);
                backGroundTop = background.advance(backGroundTop, 2);
            }

            // 2. 绘制子弹、道具与敌机
            paintObjects(canvas, enemyBullets);
            paintObjects(canvas, heroBullets);
            paintObjects(canvas, props);
            paintObjects(canvas, enemyAircraft);

            // 3. 绘制英雄机
            if (heroAircraft != null) {
                Bitmap heroBmp = ImageManager.HERO_IMAGE;
                if (heroBmp != null) {
                    canvas.drawBitmap(heroBmp,
                            heroAircraft.getLocationX() - heroBmp.getWidth() / 2f,
                            heroAircraft.getLocationY() - heroBmp.getHeight() / 2f,
                            null);
                }
            }

            // 4. 绘制得分与血量
            paintScoreAndLife(canvas);

            // 5. 游戏结束文字提示
            if (gameOverFlag) {
                mPaint.setColor(Color.RED);
                mPaint.setTextSize(90);
                mPaint.setFakeBoldText(true);
                String overText = "GAME OVER";
                float textWidth = mPaint.measureText(overText);
                canvas.drawText(overText, (screenWidth - textWidth) / 2f, screenHeight / 2f, mPaint);
            }

        } finally {
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void paintObjects(Canvas canvas, List<? extends AbstractFlyingObject> objects) {
        if (objects == null || objects.isEmpty()) return;
        for (AbstractFlyingObject object : objects) {
            Bitmap image = object.getImage();
            if (image != null) {
                canvas.drawBitmap(image,
                        object.getLocationX() - image.getWidth() / 2f,
                        object.getLocationY() - image.getHeight() / 2f,
                        null);
            }
        }
    }

    private void paintScoreAndLife(Canvas canvas) {
        int x = 40;
        int y = 90;
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(50);
        mPaint.setFakeBoldText(true);
        canvas.drawText("SCORE: " + this.score, x, y, mPaint);
        y += 65;
        if (heroAircraft != null) {
            canvas.drawText("LIFE: " + this.heroAircraft.getHp(), x, y, mPaint);
        }

        // 连击特效
        if (comboCount >= 2) {
            y += 65;
            mPaint.setColor(Color.rgb(255, 165, 0));
            mPaint.setTextSize(50 + Math.min(comboCount * 2, 20));
            canvas.drawText("COMBO x" + comboCount + " !!", x, y, mPaint);
        }
    }

    // 抽象模板方法由子类实现
    protected abstract String getDifficultyName();
    protected abstract int getHeroHp();
    protected abstract int getEnemyMaxNumber();
    protected abstract void updateDifficulty();
    protected abstract void spawnBoss();
    protected abstract AbstractAircraft produceEnemy();
}
