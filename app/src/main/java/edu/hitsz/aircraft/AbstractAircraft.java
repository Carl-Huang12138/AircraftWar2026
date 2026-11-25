package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.prop.PropObserver;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.strategy.ShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * 所有种类飞机的抽象父类
 * @author hitsz
 */
public abstract class AbstractAircraft extends AbstractFlyingObject implements PropObserver{

    //最大生命值
    protected int maxHp;
    protected int hp;
    protected volatile ShootStrategy shootStrategy;

    public AbstractAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
    }

    public void decreaseHp(int decrease){
        hp -= decrease;
        if(hp <= 0){
            hp=0;
            vanish();
        }
    }

    /**
     * 增加生命值，但不超过最大生命值
     * @param increase 增加的血量
     */
    public void increaseHp(int increase) {
        hp += increase;
        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    public int getHp() {
        return hp;
    }

    /** 仅在生成敌机时使用；与拾取补血的上限逻辑分开。 */
    public void scaleInitialHp(double multiplier) {
        maxHp = Math.max(1, (int) Math.round(maxHp * multiplier));
        hp = maxHp;
    }

    /**
     * 飞机射击方法 (统一交由策略类执行)
     * @return 子弹列表
     */
    public List<BaseBullet> shoot() {
        if (shootStrategy != null) {
            return shootStrategy.shoot(this);
        }
        return new java.util.LinkedList<>();
    }

    @Override
    public void update(AbstractProp prop) {
        // 默认空实现，这样 HeroAircraft 就不需要被迫重写它了
    }

    /**
     * 飞机坠毁时掉落道具的方法
     * 默认不掉落任何道具（返回空列表），需要的子类自行重写
     */
    public List<AbstractProp> dropProps() {
        return new LinkedList<>();
    }

    public void setShootStrategy(ShootStrategy shootStrategy) {
        this.shootStrategy = shootStrategy;
    }
}
