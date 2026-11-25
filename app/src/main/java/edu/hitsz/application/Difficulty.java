package edu.hitsz.application;

/** Intent 中的稳定标识与菜单显示名称。 */
public enum Difficulty {
    EASY("简单模式"), MEDIUM("普通模式"), HARD("困难模式");
    private final String label;
    Difficulty(String label) { this.label = label; }
    public String getLabel() { return label; }
    public static Difficulty fromExtra(String value) {
        if (value != null) {
            for (Difficulty difficulty : values()) {
                if (difficulty.name().equals(value)) return difficulty;
            }
        }
        return EASY;
    }
}
