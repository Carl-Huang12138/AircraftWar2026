package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 英雄机散射策略：同时发射3颗子弹
 */
public class HeroScatterShootStrategy implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() - 2;
        int speedY = aircraft.getSpeedY() - 5; // 子弹向上飞
        int power = 30; // 英雄机威力

        // 3 发子弹：左斜、直射、右斜 (赋予不同的横向速度 speedX)
        res.add(new HeroBullet(x, y, -2, speedY, power));
        res.add(new HeroBullet(x, y, 0, speedY, power));
        res.add(new HeroBullet(x, y, 2, speedY, power));

        return res;
    }
}