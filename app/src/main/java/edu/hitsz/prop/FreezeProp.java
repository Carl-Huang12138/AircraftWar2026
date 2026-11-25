package edu.hitsz.prop;

/**
 * 冰冻道具
 * 效果：通知所有观察者触发冰冻效果（冻结或减速敌机）
 */
public class FreezeProp extends AbstractProp {

    public FreezeProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active() {

        notifyObservers();
    }
}
