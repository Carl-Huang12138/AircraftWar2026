package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.HeroRingShootStrategy;
import edu.hitsz.strategy.HeroShootStrategy;

public class BulletPlusProp extends AbstractProp {

    public BulletPlusProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active() {
        Runnable r = () -> {
            try {
                HeroAircraft hero = HeroAircraft.getInstance();

                // 切换为环射策略
                hero.setShootStrategy(new HeroRingShootStrategy());

                // 计算本次道具应该结束的时间
                long myEndTime = System.currentTimeMillis() + 5000;

                // 把这个时间盖章到英雄机身上（如果玩家连续吃，旧时间会被这行代码覆盖更新）
                hero.setPowerUpEndTime(myEndTime);

                System.out.println("FirePlusSupply active! 英雄机开启全屏环射模式！");

                // 线程安心睡 5 秒
                Thread.sleep(5000);

                if (hero.getHp() <= 0 || hero.notValid()) {
                    // 如果英雄机已经坠毁（游戏结束），不再执行后续操作
                    return;
                }
                // 减去 50 毫秒是为了防止极其微小的线程唤醒误差导致判断失败
                if (System.currentTimeMillis() >= hero.getPowerUpEndTime() - 50) {
                    hero.setShootStrategy(new HeroShootStrategy());
                    System.out.println("超级火力道具时间到，英雄机恢复直射模式！");
                } else {
                    System.out.println("检测到超级火力已续期，老线程直接退出。");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        // 从 Runnable 实例构造一个 Thread 对象并启动线程
        new Thread(r, "超级火力道具计时线程").start();
    }
}