package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import edu.hitsz.R;
import edu.hitsz.aircraft.AceAircraft;
import edu.hitsz.aircraft.AdvancedAircraft;
import edu.hitsz.aircraft.BossAircraft;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.BulletProp;
import edu.hitsz.prop.BulletPlusProp;
import edu.hitsz.prop.FreezeProp;

import java.util.HashMap;
import java.util.Map;

/**
 * 综合管理图片的加载与访问
 * 提供图片的静态访问方法 (Android 版，BitmapFactory 缓存)
 * @author hitsz
 */
public class ImageManager {

    private static final Map<String, Bitmap> CLASSNAME_IMAGE_MAP = new HashMap<>();

    public static Bitmap BACKGROUND_IMAGE;
    public static Bitmap BACKGROUND_IMAGE_EASY;
    public static Bitmap BACKGROUND_IMAGE_MEDIUM;
    public static Bitmap BACKGROUND_IMAGE_HARD;
    public static Bitmap HERO_IMAGE;
    public static Bitmap HERO_BULLET_IMAGE;
    public static Bitmap ENEMY_BULLET_IMAGE;
    public static Bitmap MOB_ENEMY_IMAGE;
    public static Bitmap ELITE_ENEMY_IMAGE;
    public static Bitmap ELITE_PLUS_IMAGE;
    public static Bitmap ELITE_PRO_IMAGE;
    public static Bitmap PROP_BLOOD_IMAGE;
    public static Bitmap PROP_BOMB_IMAGE;
    public static Bitmap PROP_BULLET_IMAGE;
    public static Bitmap PROP_BULLET_PLUS_IMAGE;
    public static Bitmap PROP_FREEZE_IMAGE;
    public static Bitmap BOSS_ENEMY_IMAGE;

    public static void initImage(Context context) {
        if (HERO_IMAGE != null) {
            return; // 已经初始化过，避免重复加载
        }
        Context appContext = context.getApplicationContext();

        BACKGROUND_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bg);
        BACKGROUND_IMAGE_EASY = BACKGROUND_IMAGE;
        BACKGROUND_IMAGE_MEDIUM = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bg2);
        BACKGROUND_IMAGE_HARD = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bg3);

        HERO_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.hero);
        MOB_ENEMY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.mob);
        HERO_BULLET_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bullet_hero);
        ENEMY_BULLET_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bullet_enemy);
        ELITE_ENEMY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.elite);
        ELITE_PLUS_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.elite_plus);
        ELITE_PRO_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.elite_pro);
        BOSS_ENEMY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.boss);
        PROP_BLOOD_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_blood);
        PROP_BOMB_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_bomb);
        PROP_BULLET_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_bullet);
        PROP_BULLET_PLUS_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_bullet_plus);
        PROP_FREEZE_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_freeze);

        CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
        CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(), MOB_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(), HERO_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(), ENEMY_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EliteEnemy.class.getName(), ELITE_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(AdvancedAircraft.class.getName(), ELITE_PLUS_IMAGE);
        CLASSNAME_IMAGE_MAP.put(AceAircraft.class.getName(), ELITE_PRO_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BossAircraft.class.getName(), BOSS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BloodProp.class.getName(), PROP_BLOOD_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BombProp.class.getName(), PROP_BOMB_IMAGE);
        CLASSNAME_IMAGE_MAP.put(FreezeProp.class.getName(), PROP_FREEZE_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BulletProp.class.getName(), PROP_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BulletPlusProp.class.getName(), PROP_BULLET_PLUS_IMAGE);
    }

    public static Bitmap get(String className) {
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Bitmap get(Object obj) {
        if (obj == null) {
            return null;
        }
        return get(obj.getClass().getName());
    }

    public static Bitmap getBackgroundImage(String difficulty) {
        if ("EASY".equals(difficulty)) {
            return BACKGROUND_IMAGE_EASY;
        } else if ("HARD".equals(difficulty)) {
            return BACKGROUND_IMAGE_HARD;
        } else {
            return BACKGROUND_IMAGE_MEDIUM;
        }
    }
}
