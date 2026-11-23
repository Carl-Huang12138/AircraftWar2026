package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
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
        int speedY = aircraft.getSpeedY() + 5;
        int power = 15; // 王牌敌机子弹威力稍大

        // 产生 3 颗扇形子弹（带有横向 speedX 偏移）
        res.add(new EnemyBullet(x, y, -3, speedY, power)); // 左斜
        res.add(new EnemyBullet(x, y, 0, speedY, power));  // 直射
        res.add(new EnemyBullet(x, y, 3, speedY, power));  // 右斜
        return res;
    }
}
