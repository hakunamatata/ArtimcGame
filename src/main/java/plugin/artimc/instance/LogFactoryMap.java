package plugin.artimc.instance;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import plugin.artimc.engine.GameMap;

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

    public int getBaseExperience() {
        return baseExperience;
    }

    public int getWinnerExperience() {
        return baseExperience * experienceMultiple;
    }

    private int resourceTimerPeriod = 10;
    private int resourceTimerFreq = 60;
    private List<String> resourceItems;

    private int baseExperience;
    private int experienceMultiple;

    public LogFactoryMap(YamlConfiguration config, Plugin plugin) {
        super(config, plugin);
        resourceTimerPeriod = config.getInt("resources.timer.period", 10);
        resourceTimerFreq = config.getInt("resources.timer.freq", 60);
        resourceItems = config.getStringList("resources.items");
        baseExperience = config.getInt("resources.experience.base", 0);
        experienceMultiple = config.getInt("resources.experience.multiple", 0);
    }
}
