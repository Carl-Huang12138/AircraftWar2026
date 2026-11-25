package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.HeroScatterShootStrategy;

/** 三连发道具；持续时间与续期逻辑由英雄机统一管理。 */
public class BulletProp extends AbstractProp {
    public BulletProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active() {
        HeroAircraft.getInstance().activatePowerUp(new HeroScatterShootStrategy());
    }
}
