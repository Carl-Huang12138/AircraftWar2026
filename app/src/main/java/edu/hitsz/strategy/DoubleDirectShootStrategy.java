package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.CombatTuning;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import java.util.LinkedList;
import java.util.List;

public class DoubleDirectShootStrategy implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + 2;
        int speedX = 0;
        int speedY = CombatTuning.ENEMY_BULLET_SPEED; // 固定弹速，不随敌机移动或难度改变
        int power = 10; // 敌机子弹威力

        // 产生左右两排子弹
        res.add(new EnemyBullet(x - 20, y, speedX, speedY, power));
        res.add(new EnemyBullet(x + 20, y, speedX, speedY, power));
        return res;
    }
}
