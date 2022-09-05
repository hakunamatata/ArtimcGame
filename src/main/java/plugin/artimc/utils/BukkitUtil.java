package plugin.artimc.utils;

import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BukkitUtil {

    public static void addContainerItem(Container container, ItemStack item) {
        Inventory inv = container.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType().equals(Material.AIR)) {
                inv.setItem(i, item);
                break;
            }
        }
    }


}
