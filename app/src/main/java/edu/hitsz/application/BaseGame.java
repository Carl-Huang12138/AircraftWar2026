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
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 游戏数据驱动逻辑 (第 4 步接入完整逻辑)
     */
    protected void action() {
        // 后续接入生成、射击、移动与碰撞检测
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
    }

    // 抽象模板方法由子类实现
    protected abstract String getDifficultyName();
    protected abstract int getHeroHp();
    protected abstract int getEnemyMaxNumber();
    protected abstract void updateDifficulty();
    protected abstract void spawnBoss();
    protected abstract AbstractAircraft produceEnemy();
}
