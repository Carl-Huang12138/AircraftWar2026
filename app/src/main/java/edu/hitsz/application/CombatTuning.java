package edu.hitsz.application;

/** 统一的射击手感参数。速度单位：像素/逻辑帧；当前逻辑帧间隔 40ms。 */
public final class CombatTuning {
    public static final int HERO_SHOOT_CYCLE_TICKS = 6;
    public static final int HERO_BULLET_SPEED = 24;
    public static final int ENEMY_BULLET_SPEED = 12;
    public static final int HARD_BOSS_SHOOT_CYCLE_TICKS = 19;
    public static final int HARD_BOSS_HP_INCREMENT = 150;

    private CombatTuning() { }
}
