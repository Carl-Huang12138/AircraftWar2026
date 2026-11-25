package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossAircraft;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class BossFactory implements EnemyFactory {
    @Override
    public AbstractAircraft createEnemy() {
        return new BossAircraft(
                // 随机生成一个初始 X 坐标
                (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.BOSS_ENEMY_IMAGE.getWidth())),
                // 悬浮在屏幕最上方 (Y = 10% 屏幕高度的地方)
                (int) (Main.WINDOW_HEIGHT * 0.1),
                2,  // speedX: 赋予它一个横向初始速度
                0,  // speedY: 垂直速度为 0，不再下落
                500 // hp: 设定极高的血量
        );
    }
}
