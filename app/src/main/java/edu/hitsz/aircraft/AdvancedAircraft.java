package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FreezeProp;
import edu.hitsz.strategy.DoubleDirectShootStrategy;

import java.util.List;

/**
 * 精锐敌机 (ElitePlus)
 * 特点：下移 + 左右移动，发射子弹
 */
public class AdvancedAircraft extends AbstractAircraft {
    public AdvancedAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.setShootStrategy(new DoubleDirectShootStrategy());
    }

    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT ) {
            vanish();
        }
    }

    @Override
    public List<AbstractProp> dropProps() {
        // 调用精锐敌机专属掉落池 (4种道具，不含冰冻)
        return edu.hitsz.prop.PropFactory.createAdvancedProps(this.locationX, this.locationY);
    }

    @Override
    public void update(AbstractProp prop) {
        if (prop instanceof BombProp) {

            this.vanish();
        } else if (prop instanceof FreezeProp) {

            int originalSpeedX = this.speedX;
            int originalSpeedY = this.speedY;
            this.speedX = 0;
            this.speedY = 0;

            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    if (!this.notValid()) {
                        this.speedX = originalSpeedX;
                        this.speedY = originalSpeedY;

                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
}
