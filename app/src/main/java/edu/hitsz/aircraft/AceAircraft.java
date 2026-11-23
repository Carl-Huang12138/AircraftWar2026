package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FreezeProp;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.strategy.ScatterShootStrategy;

import java.util.List;

/**
 * 王牌敌机
 */
public class AceAircraft extends AdvancedAircraft {
    public AceAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.setShootStrategy(new ScatterShootStrategy());
    }

    @Override
    public List<AbstractProp> dropProps() {
        // 调用王牌敌机专属掉落池 (5种道具)
        return edu.hitsz.prop.PropFactory.createAceProps(this.locationX, this.locationY);
    }

    @Override
    public List<BaseBullet> shoot() {
        // 委托给策略去执行射击
        if (shootStrategy != null) {
            return shootStrategy.shoot(this);
        }
        return new java.util.LinkedList<>();
    }

    @Override
    public void update(AbstractProp prop) {
        if (prop instanceof BombProp) {
            System.out.println("AceAircraft 被炸弹波及，扣除 50 点血量！");
            this.decreaseHp(50);
        } else if (prop instanceof FreezeProp) {
            System.out.println("AceAircraft 受到冰冻影响，速度减半持续5秒！");
            int originalSpeedX = this.speedX;
            int originalSpeedY = this.speedY;
            this.speedX /= 2;
            this.speedY /= 2;

            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    if (!this.notValid()) {
                        this.speedX = originalSpeedX;
                        this.speedY = originalSpeedY;
                        System.out.println("AceAircraft 减速解除！");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
