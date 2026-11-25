package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import java.util.LinkedList;
import java.util.List;

public class NullShootStrategy implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        // 普通敌机不发子弹，直接返回空列表
        return new LinkedList<>();
    }
}
