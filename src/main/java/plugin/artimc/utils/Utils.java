package plugin.artimc.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class Utils {

    private Utils() {
    }

    /**
     * Removes a random element from the given list
     *
     * @param list List
     * @param <T> Type of object
     * @return Element that was removed
     */
    public static <T> T removeRandomElement(List<T> list) {
        int index = (int) (Math.random() * list.size());
        return list.remove(index);
    }

    /**
     * Returns a random element from the given list
     *
     * @param list List
     * @param <T> Type of object
     * @return Random element
     */
    public static <T> T getRandomElement(List<T> list) {
        int index = (int) (Math.random() * list.size());
        return list.get(index);
    }

    /**
     * Loads a location from a configuration section
     *
     * @param config Configuration section
     * @return Location
     */
    public static Location loadLocation(ConfigurationSection config) {
        return new Location(Bukkit.getWorld(config.getString("world")), config.getDouble("x"),
                config.getDouble("y"), config.getDouble("z"), (float) config.getDouble("yaw"),
                (float) config.getDouble("pitch"));
    }

}
