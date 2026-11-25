package edu.hitsz.prop;

/**
 * 道具效果观察者接口
 */
public interface PropObserver {
    /**
     * 当接收到道具生效的通知时，执行相应的更新逻辑
     * @param prop 触发通知的道具对象
     */
    void update(AbstractProp prop);
}
