package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;

/**
 * 敌机工厂接口 (工厂方法模式)
 */
public interface EnemyFactory {
    /**
     * 创建敌机的方法
     * @return 实例化后的具体敌机对象
     */
    AbstractAircraft createEnemy();
}
