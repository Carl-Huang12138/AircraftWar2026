package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.HeroRingShootStrategy;

/** 环射道具；持续时间与续期逻辑由英雄机统一管理。 */
public class BulletPlusProp extends AbstractProp {
    public BulletPlusProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active() {
        HeroAircraft.getInstance().activatePowerUp(new HeroRingShootStrategy());
    }
}
