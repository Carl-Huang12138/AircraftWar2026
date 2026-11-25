package edu.hitsz.strategy;

import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class ProjectilePaceTest {
    private double speed(BaseBullet bullet) {
        int x = bullet.getLocationX(), y = bullet.getLocationY();
        bullet.forward();
        return Math.hypot(bullet.getLocationX() - x, bullet.getLocationY() - y);
    }

    @Test public void enemySpeedIsFixedAcrossAircraftSpeedsAndAllPatterns() {
        Main.WINDOW_WIDTH = 1080;
        Main.WINDOW_HEIGHT = 2400;
        ShootStrategy[] patterns = {new DirectShootStrategy(), new DoubleDirectShootStrategy(),
                new ScatterShootStrategy(), new BossShootStrategy()};
        for (ShootStrategy pattern : patterns) {
            for (int aircraftSpeed : new int[]{0, 2, 5, 15}) {
                List<BaseBullet> bullets = pattern.shoot(new EliteEnemy(540, 700, 0, aircraftSpeed, 30));
                assertFalse(bullets.isEmpty());
                for (BaseBullet bullet : bullets) {
                    assertEquals(pattern.getClass().getSimpleName() + ": aircraft speed " + aircraftSpeed,
                            12, speed(bullet), 0.75);
                }
            }
        }
    }

    @Test public void normalHeroBulletUses24PixelsPerTick() {
        Main.WINDOW_WIDTH = 1080;
        Main.WINDOW_HEIGHT = 2400;
        BaseBullet bullet = new HeroShootStrategy().shoot(new EliteEnemy(540, 1200, 0, 0, 30)).get(0);
        assertEquals(24, speed(bullet), 0.001);
    }

    @Test public void tripleShotUsesNormalHeroSpeedAndKeepsThreeDirections() {
        Main.WINDOW_WIDTH = 1080;
        Main.WINDOW_HEIGHT = 2400;
        EliteEnemy stationary = new EliteEnemy(540, 1200, 0, 0, 30);
        List<BaseBullet> scatter = new HeroScatterShootStrategy().shoot(stationary);
        assertEquals(3, scatter.size());
        for (int i = 0; i < 3; i++) {
            BaseBullet bullet = scatter.get(i);
            int x = bullet.getLocationX(), y = bullet.getLocationY();
            bullet.forward();
            int dx = bullet.getLocationX() - x;
            int dy = bullet.getLocationY() - y;
            assertEquals("Triple-shot speed matches normal bullets", 24, Math.hypot(dx, dy), 0.75);
            assertEquals(Integer.signum(i - 1), Integer.signum(dx));
            assertTrue(dy < 0);
        }
    }

    @Test public void ringKeepsOriginalVelocity() {
        Main.WINDOW_WIDTH = 1080;
        Main.WINDOW_HEIGHT = 2400;
        EliteEnemy stationary = new EliteEnemy(540, 1200, 0, 0, 30);
        List<BaseBullet> ring = new HeroRingShootStrategy().shoot(stationary);
        assertEquals(20, ring.size());
        for (int i = 0; i < ring.size(); i++) {
            BaseBullet bullet = ring.get(i);
            int x = bullet.getLocationX(), y = bullet.getLocationY();
            bullet.forward();
            double angle = 2 * Math.PI * i / 20;
            assertEquals((int) (8 * Math.cos(angle)), bullet.getLocationX() - x);
            assertEquals((int) (8 * Math.sin(angle)), bullet.getLocationY() - y);
        }
    }
}
