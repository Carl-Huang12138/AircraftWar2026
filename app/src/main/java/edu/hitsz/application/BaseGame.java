package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
import edu.hitsz.prop.PropObserver;

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

    protected boolean mbLoop = false;
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
    protected double heroShootCycle = 15;
    protected int heroShootCounter = 0;
    protected double enemyShootCycle = 25;
    protected int enemyShootCounter = 0;

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
        mbLoop = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        Main.WINDOW_WIDTH = width;
        Main.WINDOW_HEIGHT = height;
        if (heroAircraft != null) {
            heroAircraft.setLocation(screenWidth / 2.0, screenHeight - 250);
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mbLoop = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
        return true;
    }

    @Override
    public void run() {
        while (mbLoop) {
            synchronized (mSurfaceHolder) {
                if (!gameOverFlag) {
                    action();
                }
                draw();
            }
            try {
                Thread.sleep(timeInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
                int enhancedHp = (int) (newEnemy.getHp() * enemyHpMultiplier);
                newEnemy.increaseHp(enhancedHp - newEnemy.getHp());

                int enhancedSpeedY = (int) (newEnemy.getSpeedY() * enemySpeedMultiplier);
                newEnemy.setSpeedY(enhancedSpeedY);

                enemyAircraft.add(newEnemy);
            }
        }
    }

    private void shootAction() {
        // 英雄机自动射击
        heroShootCounter++;
        if (heroShootCounter >= heroShootCycle) {
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
                enemyBullets.addAll(enemy.shoot());
            }
        }
    }

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

                // 英雄机与敌机撞击
                if (heroAircraft != null && (enemy.crash(heroAircraft) || heroAircraft.crash(enemy))) {
                    enemy.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
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
                        if (enemyBullet instanceof PropObserver) {
                            prop.addObserver((PropObserver) enemyBullet);
                        }
                    }
                }
                prop.active();
                prop.vanish();
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
        if (heroAircraft != null && heroAircraft.getHp() <= 0) {
            gameOverFlag = true;
        }
    }

    /**
     * 游戏画面绘制 (双缓冲 Canvas 渲染)
     */
    public void draw() {
        Canvas canvas = mSurfaceHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        try {
            // 1. 绘制滚动背景
            Bitmap bg = ImageManager.getBackgroundImage(difficulty);
            if (bg != null) {
                Rect src = new Rect(0, 0, bg.getWidth(), bg.getHeight());
                Rect dst1 = new Rect(0, backGroundTop - screenHeight, screenWidth, backGroundTop);
                Rect dst2 = new Rect(0, backGroundTop, screenWidth, backGroundTop + screenHeight);
                canvas.drawBitmap(bg, src, dst1, null);
                canvas.drawBitmap(bg, src, dst2, null);
                backGroundTop += 2;
                if (backGroundTop >= screenHeight) {
                    backGroundTop = 0;
                }
            } else {
                canvas.drawColor(Color.BLACK);
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
