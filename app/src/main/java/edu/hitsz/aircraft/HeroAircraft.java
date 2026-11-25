package edu.hitsz.aircraft;

import edu.hitsz.strategy.HeroRingShootStrategy;
import edu.hitsz.strategy.HeroShootStrategy;
import edu.hitsz.strategy.ShootStrategy;

/**
 * 英雄飞机，游戏玩家操控
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    private static final long POWER_UP_DURATION_MS = 5_000;
    private static volatile HeroAircraft instance;
    private long powerUpVersion;

    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        setShootStrategy(new HeroShootStrategy());
    }

    // 使用双重检查锁定；每局由 BaseGame 重置单例。
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

    public static synchronized void resetInstance() {
        instance = null;
    }

    //提供一个无参的获取方法，方便后续在其他类中直接获取实例，而不需要每次都传参数
    public static HeroAircraft getInstance() {
        HeroAircraft hero = instance;
        if (hero == null) {
            throw new IllegalStateException("HeroAircraft is not initialized");
        }
        return hero;
    }

    @Override
    public void forward() {
        // 英雄机由触摸控制，不通过 forward 移动。
    }

    /** 同步捕获当前英雄；计时器只修改这个对象，不能影响下一局的单例。 */
    public void activatePowerUp(ShootStrategy strategy) {
        long version = beginPowerUp(strategy);
        new Thread(() -> {
            try {
                Thread.sleep(POWER_UP_DURATION_MS);
                expirePowerUp(version);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }, "Hero-PowerUpTimer").start();
    }

    synchronized long beginPowerUp(ShootStrategy strategy) {
        setShootStrategy(strategy);
        return ++powerUpVersion;
    }

    synchronized void expirePowerUp(long version) {
        if (version == powerUpVersion && !notValid()) {
            setShootStrategy(new HeroShootStrategy());
        }
    }

    public boolean isRingShooting() {
        return shootStrategy instanceof HeroRingShootStrategy;
    }

}
