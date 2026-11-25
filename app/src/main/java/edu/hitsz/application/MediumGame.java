package edu.hitsz.application;

import android.content.Context;
import edu.hitsz.aircraft.AbstractAircraft;

/** 从原桌面版迁移普通难度，保留原有生成周期与敌机分布。 */
public class MediumGame extends BaseGame {
    public MediumGame(Context context) {
        super(context);
        ringShootCycle = 20;
        enemyShootCycle = 20;
        enemySpawnCycle = 20;
        scoreThreshold = 1000;
    }
    @Override protected String getDifficultyName() { return "MEDIUM"; }
    @Override protected int getHeroHp() { return 100; }
    @Override protected int getEnemyMaxNumber() { return 5; }
    @Override protected void updateDifficulty() {
        if (timeCount > 0 && timeCount % 10000 == 0) {
            enemySpawnCycle = Math.max(15, enemySpawnCycle - 0.5);
            enemyHpMultiplier += 0.05;
            enemySpeedMultiplier += 0.05;
        }
    }
    @Override protected void spawnBoss() {
        bossExisting = true;
        enemyAircraft.add(bossFactory.createEnemy());
        scoreThreshold = score + 1000;
        sendGameEvent(GameEvent.BOSS, 1);
    }
    @Override protected AbstractAircraft produceEnemy() {
        double value = Math.random();
        if (value < 0.1) return advancedFactory.createEnemy();
        if (value < 0.3) return eliteFactory.createEnemy();
        return mobFactory.createEnemy();
    }
}
