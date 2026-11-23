package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import java.util.List;

/**
 * 射击策略接口 (Strategy Pattern)
 */
public interface ShootStrategy {
    /**
     * 执行射击动作
     * @param aircraft 执行射击的飞机上下文（用于获取坐标、速度等状态）
     * @return 产生的子弹列表
     */
    List<BaseBullet> shoot(AbstractAircraft aircraft);
}
