package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.AdvancedAircraft;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class AdvancedFactory implements EnemyFactory {
    @Override
    public AbstractAircraft createEnemy() {
        return new AdvancedAircraft(
                (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_PLUS_IMAGE.getWidth())),
                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                3,
                8,
                80
        );
    }
}
