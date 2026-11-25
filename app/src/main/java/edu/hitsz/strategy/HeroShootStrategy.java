package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.CombatTuning;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;
import java.util.LinkedList;
import java.util.List;

public class HeroShootStrategy implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() - 2; // 子弹在飞机上方
        int speedX = 0;
        int speedY = -CombatTuning.HERO_BULLET_SPEED; // 英雄机子弹向上飞（通常为负数）
        int power = 30; // 英雄机威力

        res.add(new HeroBullet(x, y, speedX, speedY, power));
        return res;
    }
}
