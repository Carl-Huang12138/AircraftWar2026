package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 加血道具
 */
public class BloodProp extends AbstractProp {

    private int healAmount = 30;

    public BloodProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active() {
        // 获取英雄机单例
        HeroAircraft hero = HeroAircraft.getInstance();

        // 给英雄机加血
        hero.increaseHp(healAmount);

        // 打印提示信息
        System.out.println("加血道具生效！恢复血量: " + healAmount + "，英雄机当前血量: " + hero.getHp());

    }
}
