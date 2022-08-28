package plugin.artimc.engine;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;

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

    public ChatColor getChatColor() {
        switch (this) {
            case RED:
                return ChatColor.RED;
            case ORANGE:
                return ChatColor.GOLD;
            case YELLOW:
                return ChatColor.YELLOW;
            case GREEN:
                return ChatColor.GREEN;
            case LIME:
                return ChatColor.AQUA;
            case BLUE:
                return ChatColor.BLUE;
            case PURPLE:
                return ChatColor.LIGHT_PURPLE;
            default:
                return ChatColor.WHITE;
        }
    }

    public String getName() {
        switch (this) {
            case ORANGE:
                return "橙队";
            case YELLOW:
                return "黄队";
            case GREEN:
                return "绿队";
            case LIME:
                return "青队";
            case BLUE:
                return "蓝队";
            case PURPLE:
                return "紫队";
            default:
                return "红队";
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
                return "§r";
        }
    }
}
