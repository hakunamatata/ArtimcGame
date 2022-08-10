package plugin.artimc.engine;

import net.kyori.adventure.text.format.NamedTextColor;

public enum PartyName {
    /**
     * 红队 §c
     */
    RED,
    /**
     * 橙对 §6
     */
    ORANGE,
    /**
     * 黄队 §e
     */
    YELLOW,
    /**
     * 绿队 §a
     */
    GREEN,
    /**
     * 青对 §b
     */
    LIME,
    /**
     * 蓝队 §9
     */
    BLUE,
    /**
     * 紫对 §5
     */
    PURPLE;

    public NamedTextColor getNamedTextColor() {
        switch (this) {
            case RED:
                return NamedTextColor.RED;
            case ORANGE:
                return NamedTextColor.GOLD;
            case YELLOW:
                return NamedTextColor.YELLOW;
            case GREEN:
                return NamedTextColor.GREEN;
            case LIME:
                return NamedTextColor.AQUA;
            case BLUE:
                return NamedTextColor.BLUE;
            case PURPLE:
                return NamedTextColor.LIGHT_PURPLE;
            default:
                return NamedTextColor.WHITE;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case RED:
                return "§c";
            case ORANGE:
                return "§6";
            case YELLOW:
                return "§e";
            case GREEN:
                return "§a";
            case LIME:
                return "§b";
            case BLUE:
                return "§9";
            case PURPLE:
                return "§5";
            default:
                return "§f";
        }
    }
}