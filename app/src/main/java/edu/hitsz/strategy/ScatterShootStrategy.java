package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.CombatTuning;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import java.util.LinkedList;
import java.util.List;

public class ScatterShootStrategy implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + 2;
        int speedY = (int) Math.round(CombatTuning.ENEMY_BULLET_SPEED * Math.cos(Math.PI / 9));
        int sideSpeed = (int) Math.round(CombatTuning.ENEMY_BULLET_SPEED * Math.sin(Math.PI / 9));
        int power = 15; // 王牌敌机子弹威力稍大

        // 产生 3 颗扇形子弹（带有横向 speedX 偏移）
        res.add(new EnemyBullet(x, y, -sideSpeed, speedY, power)); // 左斜
        res.add(new EnemyBullet(x, y, 0, CombatTuning.ENEMY_BULLET_SPEED, power));  // 直射
        res.add(new EnemyBullet(x, y, sideSpeed, speedY, power));  // 右斜
        return res;
    }
}
