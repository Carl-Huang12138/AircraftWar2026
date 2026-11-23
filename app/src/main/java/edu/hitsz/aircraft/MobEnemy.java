package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FreezeProp;

import java.util.LinkedList;
import java.util.List;

/**
 * 普通敌机
 * 不可射击、不掉落道具
 * @author hitsz
 */
public class MobEnemy extends AbstractAircraft {

    public MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.setShootStrategy(new edu.hitsz.strategy.NullShootStrategy());
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
    public void update(AbstractProp prop) {
        if (prop instanceof BombProp) {
            System.out.println("MobEnemy 观察到炸弹，原地销毁！");
            this.vanish();
        } else if (prop instanceof FreezeProp) {
            System.out.println("MobEnemy 观察到冰冻道具，永久冻结！");
            this.speedX = 0;
            this.speedY = 0;
        }
    }


}
