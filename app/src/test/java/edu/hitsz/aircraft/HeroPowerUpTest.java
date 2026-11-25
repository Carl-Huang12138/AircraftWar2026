package edu.hitsz.aircraft;

import edu.hitsz.strategy.HeroRingShootStrategy;
import edu.hitsz.strategy.HeroScatterShootStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** 直接执行计时器的开始/到期回调，避免真实等待五秒。 */
public class HeroPowerUpTest {
    private HeroAircraft hero;

    @Before public void setUp() {
        HeroAircraft.resetInstance();
        hero = HeroAircraft.getInstance(300, 600, 0, 0, 100);
    }

    @After public void tearDown() { HeroAircraft.resetInstance(); }

    @Test public void latestPickupSurvivesAnOlderExpiryCallback() {
        long scatter = hero.beginPowerUp(new HeroScatterShootStrategy());
        assertEquals(3, hero.shoot().size());
        long ring = hero.beginPowerUp(new HeroRingShootStrategy());
        hero.expirePowerUp(scatter);
        assertEquals(20, hero.shoot().size());
        hero.expirePowerUp(ring);
        assertEquals(1, hero.shoot().size());
    }

    @Test public void previousRoundTimerCannotChangeTheNewHero() {
        long previousTimer = hero.beginPowerUp(new HeroRingShootStrategy());
        HeroAircraft.resetInstance();
        HeroAircraft nextHero = HeroAircraft.getInstance(300, 600, 0, 0, 100);
        nextHero.beginPowerUp(new HeroScatterShootStrategy());
        hero.expirePowerUp(previousTimer);
        assertEquals(3, nextHero.shoot().size());
    }
}
