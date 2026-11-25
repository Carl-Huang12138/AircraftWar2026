package edu.hitsz.application;

import android.content.Context;
import edu.hitsz.aircraft.AbstractAircraft;

/** 困难模式包含王牌敌机，难度每十秒递进。 */
public class HardGame extends BaseGame {
    private double eliteProbability = 0.35;
    private int bossLevel = 1;
    public HardGame(Context context) {
        super(context);
        ringShootCycle = 22;
        enemyShootCycle = 18;
        enemySpawnCycle = 18;
        scoreThreshold = 800;
    }
    @Override protected String getDifficultyName() { return "HARD"; }
    @Override protected int getHeroHp() { return 80; }
    @Override protected int getEnemyMaxNumber() { return 6; }
    @Override protected void updateDifficulty() {
        if (timeCount > 0 && timeCount % 10000 == 0) {
            enemySpawnCycle = Math.max(12, enemySpawnCycle - 0.5);
            enemyShootCycle = Math.max(15, enemyShootCycle - 0.5);
            ringShootCycle = Math.min(25, ringShootCycle + 0.5);
            enemyHpMultiplier += 0.08;
            enemySpeedMultiplier += 0.08;
            eliteProbability = Math.min(0.6, eliteProbability + 0.05);
        }
    }
    @Override protected void spawnBoss() {
        bossExisting = true;
        AbstractAircraft boss = bossFactory.createEnemy();
        boss.scaleInitialHp(1 + (double) CombatTuning.HARD_BOSS_HP_INCREMENT * bossLevel / boss.getHp());
        bossLevel++;
        enemyAircraft.add(boss);
        scoreThreshold = score + 800;
        sendGameEvent(GameEvent.BOSS, 1);
    }
    @Override protected double getBossShootCycle() {
        return CombatTuning.HARD_BOSS_SHOOT_CYCLE_TICKS;
    }
    @Override protected AbstractAircraft produceEnemy() {
        double value = Math.random();
        if (value < 0.05) return aceFactory.createEnemy();
        if (value < 0.2) return advancedFactory.createEnemy();
        if (value < eliteProbability) return eliteFactory.createEnemy();
        return mobFactory.createEnemy();
    }
}
