package plugin.artimc.instance;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import plugin.artimc.engine.GameMap;

import java.util.ArrayList;
import java.util.List;

public class LogFactoryMap extends GameMap {

    public int getResourceTimerPeriod() {
        return resourceTimerPeriod;
    }

    public int getResourceTimerFreq() {
        return resourceTimerFreq;
    }

    public List<String> getResourceItems() {
        return resourceItems;
    }

    private int resourceTimerPeriod = 10;
    private int resourceTimerFreq = 60;
    private List<String> resourceItems = new ArrayList<>();

    public LogFactoryMap(YamlConfiguration config, Plugin plugin) {
        super(config, plugin);
        resourceTimerPeriod = config.getInt("resources.timer.period", 10);
        resourceTimerFreq = config.getInt("resources.timer.freq", 60);
        resourceItems = config.getStringList("resources.items");
    }
}
