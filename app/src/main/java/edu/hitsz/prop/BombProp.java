package edu.hitsz.prop;

/**
 * 炸弹道具
 * 效果：通知所有观察者触发炸弹效果（清屏普通敌机、对王牌敌机造成伤害、清除敌机子弹）
 */
public class BombProp extends AbstractProp {

    public BombProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active() {
        System.out.println("BombProp active: 炸弹道具生效！");

        // 通知所有已经注册的观察者
        notifyObservers();
    }
}
