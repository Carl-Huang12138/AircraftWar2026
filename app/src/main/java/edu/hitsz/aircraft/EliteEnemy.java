package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FreezeProp;

import java.util.List;

/**
 * 精英敌机
 * 特点：向下直线移动，会以一定频率向下发射敌机子弹，被击毁后可掉落道具
 * @author hitsz
 */
public class EliteEnemy extends AbstractAircraft {

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.setShootStrategy(new edu.hitsz.strategy.DirectShootStrategy());
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
        // 调用精英敌机专属掉落池 (3种道具)
        return edu.hitsz.prop.PropFactory.createEliteProps(this.getLocationX(), this.getLocationY());
    }

    @Override
    public void update(AbstractProp prop) {
        if (prop instanceof BombProp) {

            this.vanish();
        } else if (prop instanceof FreezeProp) {

            // 1. 记录原始速度
            int originalSpeedX = this.speedX;
            int originalSpeedY = this.speedY;
            // 2. 冻结
            this.speedX = 0;
            this.speedY = 0;

            // 3. 开启线程，4秒后解冻
            new Thread(() -> {
                try {
                    Thread.sleep(4000); // 精锐机 AdvancedAircraft 这里改成 3000
                    // 解冻前判断一下飞机是不是已经被打死了，没死才恢复速度
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
