package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.CombatTuning;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import java.util.LinkedList;
import java.util.List;

public class DirectShootStrategy implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + 2; // 子弹在飞机下方
        int speedX = 0;
        int speedY = CombatTuning.ENEMY_BULLET_SPEED; // 敌机子弹向下飞
        int power = 10;

        res.add(new EnemyBullet(x, y, speedX, speedY, power));
        return res;
    }
}
