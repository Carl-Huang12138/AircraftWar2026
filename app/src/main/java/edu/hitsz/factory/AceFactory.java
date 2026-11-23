package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.AceAircraft;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class AceFactory implements EnemyFactory {
    @Override
    public AbstractAircraft createEnemy() {
        return new AceAircraft(
                (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_PRO_IMAGE.getWidth())),
                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                5,
                6,
                120
        );
    }
}
