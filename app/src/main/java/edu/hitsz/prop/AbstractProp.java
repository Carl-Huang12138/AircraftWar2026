package edu.hitsz.prop;

import edu.hitsz.application.Main;
import edu.hitsz.basic.AbstractFlyingObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 道具抽象基类
 * 充当观察者模式中的 Subject (被观察的目标)
 */
public abstract class AbstractProp extends AbstractFlyingObject {

    // 维护一个观察者列表，里面存放当前屏幕上的敌机和敌机子弹
    private List<PropObserver> observers = new ArrayList<>();

    public AbstractProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    // 观察者模式核心方法

    // 注册观察者
    public void addObserver(PropObserver observer) {
        observers.add(observer);
    }

    // 移除观察者
    public void removeObserver(PropObserver observer) {
        observers.remove(observer);
    }

    // 通知所有观察者
    public void notifyObservers() {
        for (PropObserver observer : observers) {
            observer.update(this);
        }
    }

    /**
     * 道具生效的抽象方法
     * 具体的生效逻辑交由各个子类实现
     */
    public abstract void active();

    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }
}