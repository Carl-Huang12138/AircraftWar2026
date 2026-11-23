package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 英雄飞机，游戏玩家操控
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    private long powerUpEndTime = 0;

    private volatile static HeroAircraft instance = null;
    //每次射击发射子弹数量
    private int shootNum = 1;

    //子弹威力
    private int power = 30;

    //子弹射击方向 (向上发射：-1，向下发射：1)
    private int direction = -1;

    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.setShootStrategy(new edu.hitsz.strategy.HeroShootStrategy());
    }

    // 3. 提供全局唯一的访问点 (使用双重检查锁定 DCL)
    public static HeroAircraft getInstance(int locationX, int locationY, int speedX, int speedY, int hp) {
        if (instance == null) {
            synchronized (HeroAircraft.class) {
                if (instance == null) {
                    instance = new HeroAircraft(locationX, locationY, speedX, speedY, hp);
                }
            }
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    //提供一个无参的获取方法，方便后续在其他类中直接获取实例，而不需要每次都传参数
    public static HeroAircraft getInstance() {
        if (instance == null) {
            throw new RuntimeException("HeroAircraft is not initialized! Please call getInstance with parameters first.");
        }
        return instance;
    }

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不通过forward函数移动
    }

    public void setPowerUpEndTime(long powerUpEndTime) {
        this.powerUpEndTime = powerUpEndTime;
    }

    public long getPowerUpEndTime() {
        return powerUpEndTime;
    }

}
