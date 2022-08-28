package plugin.artimc.utils;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.List;

public class GameUtil {

    /**
     * 删除一个世界的所有物品
     *
     * @param world
     */
    public static void clearDropItems(World world) {
        List<Entity> list = world.getEntities();
        for (Entity current : list) {
            if (current instanceof Item) {
                current.remove();
            }
        }
    }

}
