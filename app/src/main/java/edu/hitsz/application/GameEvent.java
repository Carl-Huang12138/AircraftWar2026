package edu.hitsz.application;

/** 游戏子线程只发送数据，由 Activity 的主线程 Handler 处理 UI 与音频。 */
public final class GameEvent {
    public static final int HIT = 1;
    public static final int SUPPLY = 2;
    public static final int BOMB = 3;
    public static final int BOSS = 4;
    public static final int GAME_OVER = 5;
    private GameEvent() { }
}
