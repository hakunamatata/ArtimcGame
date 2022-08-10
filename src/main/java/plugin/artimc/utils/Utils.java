package plugin.artimc.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class Utils {

    private Utils() {
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
