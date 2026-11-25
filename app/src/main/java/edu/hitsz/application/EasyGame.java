package edu.hitsz.application;

import android.content.Context;

import edu.hitsz.aircraft.AbstractAircraft;

/**
 * 简单难度游戏模式
 * @author hitsz
 */
public class EasyGame extends BaseGame {

    public EasyGame(Context context) {
        super(context);
        this.enemyShootCycle = 30;
        this.enemySpawnCycle = 30;
    }

    @Override
    protected String getDifficultyName() {
        return "EASY";
    }

    @Override
    protected int getHeroHp() {
        return 150;
    }

    @Override
    protected int getEnemyMaxNumber() {
        return 3;
    }

    @Override
    protected void updateDifficulty() {
        // 简单模式难度不随时间变化
    }

    @Override
    protected void spawnBoss() {
        // 简单模式不生成 Boss
    }

    @Override
    protected AbstractAircraft produceEnemy() {
        // 简单模式：普通敌机和少量精英敌机
        double rand = Math.random();
        if (rand < 0.2) {
            return eliteFactory.createEnemy();
        } else {
            return mobFactory.createEnemy();
        }
    }
}
