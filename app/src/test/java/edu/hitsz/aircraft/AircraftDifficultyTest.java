package edu.hitsz.aircraft;

import org.junit.Test;
import static org.junit.Assert.*;

public class AircraftDifficultyTest {
    @Test public void difficultyRaisesInitialHealthAndHealingCapTogether() {
        MobEnemy enemy = new MobEnemy(10, 10, 0, 1, 30);
        enemy.scaleInitialHp(1.5);
        assertEquals(45, enemy.getHp());
        enemy.decreaseHp(10);
        enemy.increaseHp(100);
        assertEquals(45, enemy.getHp());
    }
}
