package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.CombatTuning;
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
        int speed = CombatTuning.HERO_BULLET_SPEED;
        // 保留原来左右分量2:向上分量5的散射角度，统一为普通弹的标称速率。
        int sideX = (int) Math.round(speed * 2 / Math.sqrt(29));
        int sideY = -(int) Math.round(speed * 5 / Math.sqrt(29));
        int power = 30; // 英雄机威力

        // 3 发子弹：左斜、直射、右斜 (赋予不同的横向速度 speedX)
        res.add(new HeroBullet(x, y, -sideX, sideY, power));
        res.add(new HeroBullet(x, y, 0, -speed, power));
        res.add(new HeroBullet(x, y, sideX, sideY, power));

        return res;
    }
}
