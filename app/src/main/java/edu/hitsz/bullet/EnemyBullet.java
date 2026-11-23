package edu.hitsz.bullet;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FreezeProp;
import edu.hitsz.prop.PropObserver;

/**
 * 敌机子弹
 * @Author hitsz
 */
public class EnemyBullet extends BaseBullet implements PropObserver {

    public EnemyBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
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
                    Thread.sleep(5000);
                    if (!this.notValid()) {
                        this.speedX = originalSpeedX;
                        this.speedY = originalSpeedY;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
