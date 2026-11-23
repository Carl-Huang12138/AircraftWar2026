package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.PropFactory;
import edu.hitsz.strategy.BossShootStrategy;

import java.util.List;

public class BossAircraft extends AbstractAircraft {

    public BossAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 装配专属环射策略
        this.setShootStrategy(new BossShootStrategy());
    }

    /**
     * 重写移动逻辑：悬浮于上方，左右折返
     */
    @Override
    public void forward() {
        // 仅在 X 轴上移动 (speedY 为 0)
        locationX += speedX;

        // 当碰到屏幕左右边缘时，反转速度方向实现折返
        if (locationX <= 0 || locationX >= Main.WINDOW_WIDTH) {
            speedX = -speedX;
        }
    }

    /**
     * 调用之前写好的 Boss 专属掉落池 (3个道具)
     */
    @Override
    public List<AbstractProp> dropProps() {
        return PropFactory.createBossProps(this.getLocationX(), this.getLocationY());
    }
}