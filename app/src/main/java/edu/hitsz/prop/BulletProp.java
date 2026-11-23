package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.HeroScatterShootStrategy;
import edu.hitsz.strategy.HeroShootStrategy;

public class BulletProp extends AbstractProp {

    public BulletProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active() {
        Runnable r = () -> {
            try {
                HeroAircraft hero = HeroAircraft.getInstance();
                hero.setShootStrategy(new HeroScatterShootStrategy());

                // 计算本次道具应该结束的时间
                long myEndTime = System.currentTimeMillis() + 5000;

                // 把这个时间盖章到英雄机身上
                hero.setPowerUpEndTime(myEndTime);

                System.out.println("FireSupply active! 英雄机开启散射模式！");

                // 线程安心睡 5 秒
                Thread.sleep(5000);

                if (hero.getHp() <= 0 || hero.notValid()) {
                    // 如果英雄机已经坠毁（游戏结束），线程直接提前下班，不再执行后续操作
                    return;
                }

                // 如果英雄机身上的时间比 myEndTime 大，说明玩家在期间吃到了新道具，本线程无需恢复直射，直接结束即可！
                if (System.currentTimeMillis() >= hero.getPowerUpEndTime() - 50) {
                    hero.setShootStrategy(new HeroShootStrategy());
                    System.out.println("火力道具时间到，且期间未获取新道具，英雄机恢复直射模式！");
                } else {
                    System.out.println("检测到火力已续期，老线程功成身退，不执行恢复直射操作。");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        new Thread(r, "火力道具计时线程").start();
    }
}