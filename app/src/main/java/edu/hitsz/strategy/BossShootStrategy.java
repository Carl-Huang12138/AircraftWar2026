package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.CombatTuning;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * Boss 敌机专属策略：20颗子弹全屏环射
 */
public class BossShootStrategy implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + 2; // 从 Boss 下方发射
        int power = 20; // Boss 子弹威力
        int baseSpeed = CombatTuning.ENEMY_BULLET_SPEED; // 子弹的散开速度
        int bulletNum = 20;

        for (int i = 0; i < bulletNum; i++) {
            // 将 360 度平分为 20 份
            double angle = 2 * Math.PI * i / bulletNum;
            // 利用三角函数计算四周的偏移速度
            int speedX = (int) Math.round(baseSpeed * Math.cos(angle));
            int speedY = (int) Math.round(baseSpeed * Math.sin(angle));

            res.add(new EnemyBullet(x, y, speedX, speedY, power));
        }
        return res;
    }
}
