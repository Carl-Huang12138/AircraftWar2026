package edu.hitsz.prop;

import java.util.LinkedList;
import java.util.List;

/**
 * 道具简单工厂
 */
public class PropFactory {

    /**
     * 1. 精英敌机专属掉落池 (EliteEnemy)
     * 规则：100% 掉落，仅包含 3 种道具（加血、火力、超级火力）
     */
    public static List<AbstractProp> createEliteProps(int x, int y) {
        List<AbstractProp> res = new LinkedList<>();
        // 直接在 3 种道具中随机选一个 (0, 1, 2)
        int type = (int) (Math.random() * 3);
        switch (type) {
            case 0:
                res.add(new BloodProp(x, y, 0, 5));
                break;
            case 1:
                res.add(new BulletProp(x, y, 0, 5));
                break;
            case 2:
                res.add(new BulletPlusProp(x, y, 0, 5));
                break;
            default:
                break;
        }
        return res;
    }

    /**
     * 2. 精锐敌机专属掉落池 (AdvancedAircraft)
     * 包含 4 种道具（不含冰冻）
     */
    public static List<AbstractProp> createAdvancedProps(int x, int y) {
        List<AbstractProp> res = new LinkedList<>();
        // 直接在 4 种道具中随机选一个 (0, 1, 2, 3)
        int type = (int) (Math.random() * 4);
        switch (type) {
            case 0:
                res.add(new BloodProp(x, y, 0, 5));
                break;
            case 1:
                res.add(new BombProp(x, y, 0, 5));
                break;
            case 2:
                res.add(new BulletProp(x, y, 0, 5));
                break;
            case 3:
                res.add(new BulletPlusProp(x, y, 0, 5));
                break;
            default:
                break;
        }
        return res;
    }

    /**
     * 3. 王牌敌机专属掉落池 (AceAircraft)
     */
    public static List<AbstractProp> createAceProps(int x, int y) {
        List<AbstractProp> res = new LinkedList<>();
        // 直接在 5 种道具中随机选一个 (0, 1, 2, 3, 4)
        int type = (int) (Math.random() * 5);
        switch (type) {
            case 0:
                res.add(new BloodProp(x, y, 0, 5));
                break;
            case 1:
                res.add(new BombProp(x, y, 0, 5));
                break;
            case 2:
                res.add(new FreezeProp(x, y, 0, 5));
                break;
            case 3:
                res.add(new BulletProp(x, y, 0, 5));
                break;
            case 4:
                res.add(new BulletPlusProp(x, y, 0, 5));
                break;
            default:
                break;
        }
        return res;
    }

    /**
     * 4. Boss 敌机专属掉落池 (BossAircraft)
     * 规则：100% 掉落，一次性生成 3 个道具，5 种道具全包含
     */
    public static List<AbstractProp> createBossProps(int x, int y) {
        List<AbstractProp> res = new LinkedList<>();

        // 循环 3 次，生成 3 个道具
        for (int i = 0; i < 3; i++) {
            // 为了防止 3 个道具完全重叠，计算一个横向偏移量
            // 当 i 分别为 0, 1, 2 时，offsetX 分别为 -30, 0, 30
            int offsetX = (i - 1) * 30;

            // 在 5 种道具中随机选一个 (0, 1, 2, 3, 4)
            int type = (int) (Math.random() * 5);
            switch (type) {
                case 0:
                    res.add(new BloodProp(x + offsetX, y, 0, 5));
                    break;
                case 1:
                    res.add(new BombProp(x + offsetX, y, 0, 5));
                    break;
                case 2:
                    res.add(new FreezeProp(x + offsetX, y, 0, 5));
                    break;
                case 3:
                    res.add(new BulletProp(x + offsetX, y, 0, 5));
                    break;
                case 4:
                    res.add(new BulletPlusProp(x + offsetX, y, 0, 5));
                    break;
                default:
                    break;
            }
        }
        return res; // 返回包含 3 个道具的列表
    }
}
