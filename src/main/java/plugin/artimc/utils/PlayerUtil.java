package plugin.artimc.utils;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerUtil {

    public static String serializeItemStack(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("i", itemStack);
        return config.saveToString();
    }

    public static ItemStack deserializeItemStack(String value) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(value);
            return config.getItemStack("i", null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String serializerInventory(Player player) {
        StringBuilder stringBuilder = new StringBuilder();
        PlayerInventory inv = player.getInventory();
        YamlConfiguration yml = new YamlConfiguration();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) ? new ItemStack(Material.AIR) : inv.getItem(i);
            yml.set(String.valueOf(i), item);
        }
        yml.set("size", inv.getSize());
        return yml.saveToString();
    }

    public static void deserializeInventory(Player player, String values) {
        YamlConfiguration yml = new YamlConfiguration();
        try {
            yml.loadFromString(values);
            for (int i = 0; i < yml.getInt("size", 0); i++) {
                player.getInventory().setItem(i, yml.getItemStack(String.valueOf(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
