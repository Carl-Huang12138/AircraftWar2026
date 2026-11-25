package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 英雄机环射策略：同时向四周发射20颗子弹
 */
public class HeroRingShootStrategy implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY();
        int power = 30;

        int baseSpeed = 8; // 设定环射子弹的基础散开速度
        int bulletNum = 20; // 20 颗子弹

        for (int i = 0; i < bulletNum; i++) {
            // 将 360 度 (2π) 等分为 20 份
            double angle = 2 * Math.PI * i / bulletNum;

            // 根据角度利用三角函数计算 x 和 y 方向的分速度
            int speedX = (int) (baseSpeed * Math.cos(angle));
            int speedY = (int) (baseSpeed * Math.sin(angle));

            res.add(new HeroBullet(x, y, speedX, speedY, power));
        }
        return res;
    }
}
