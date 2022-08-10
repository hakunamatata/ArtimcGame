package plugin.artimc.engine;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import plugin.artimc.utils.Utils;

public class GameMap {

    protected Plugin plugin;
    protected YamlConfiguration config;
    private String name;
    private String description;
    private boolean active;
    private Map<String, Location> spawn;
    private int maxParties = 2;
    private int maxMembers = 5;
    private String worldName = "";
    private String schemaFile = "";
    private int baseHeight = 64;
    private World.Environment environment = World.Environment.NORMAL;
    private int waitPeriod = 120;
    private GameType type = GameType.PVP;
    private String scoreboardTitle = "";
    private String startTitle = "游戏开始";
    private String startSubTitle = "";
    private String finishTitle = "游戏结束";
    private String finishSubTitle = "";
    private String winTitle = "";
    private String winSubTitle = "";
    private String looseTitle = "";
    private String looseSubTitle = "";

    public Plugin getPlugin() {
        return plugin;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public int getBaseHeight() {
        return baseHeight;
    }

    public World.Environment getEnvironment() {
        return environment;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getSchemaFile() {
        return schemaFile;
    }

    public String getStartTitle() {
        return startTitle;
    }

    public String getStartSubTitle() {
        return startSubTitle;
    }

    public String getFinishTitle() {
        return finishTitle;
    }

    public String getFinishSubTitle() {
        return finishSubTitle;
    }

    public String getScoreboardTitle() {
        return scoreboardTitle;
    }

    public GameType getType() {
        return type;
    }

    public int getMaxParties() {
        return maxParties;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public int getWaitPeriod() {
        return waitPeriod;
    }

    public int getGamePeriod() {
        return gamePeriod;
    }

    public int getFinishPeriod() {
        return finishPeriod;
    }

    public String getWinTitle() {
        return winTitle;
    }

    public String getWinSubTitle() {
        return winSubTitle;
    }

    public String getLooseTitle() {
        return looseTitle;
    }

    public String getLooseSubTitle() {
        return looseSubTitle;
    }

    public Location getLobby() {
        return getLocation(plugin.getConfig().getConfigurationSection("lobby"));
    }

    private int gamePeriod = 120;
    private int finishPeriod = 10;

    public GameMap(YamlConfiguration config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
        name = config.getString("name");
        description = config.getString("desc");
        active = config.getBoolean("active");
        maxParties = config.getInt("max-parties", 2);
        maxMembers = config.getInt("max-members", 5);
        worldName = config.getString("world-name");
        schemaFile = config.getString("schema-file");
        baseHeight = config.getInt("base-height", 64);
        environment = World.Environment.valueOf(config.getString("environment", "normal").toUpperCase());

        type = GameType.valueOf(config.getString("type", "pvp").toUpperCase());
        spawn = new HashMap<>();
        spawn.put("default", getLocation(config.getConfigurationSection("spawn.default")));
        spawn.put("host", getLocation(config.getConfigurationSection("spawn.host")));
        spawn.put("guest", getLocation(config.getConfigurationSection("spawn.guest")));
        scoreboardTitle = ChatColor.translateAlternateColorCodes('&', config.getString("scoreboard.title", ""));
        startTitle = ChatColor.translateAlternateColorCodes('&', config.getString("titles.start.title", ""));
        startSubTitle = ChatColor.translateAlternateColorCodes('&', config.getString("titles.start.subtitle", ""));
        finishTitle = ChatColor.translateAlternateColorCodes('&', config.getString("titles.finish.title", ""));
        finishSubTitle = ChatColor.translateAlternateColorCodes('&', config.getString("titles.finish.subtitle", ""));

        winTitle = ChatColor.translateAlternateColorCodes('&', config.getString("titles.win.title", ""));
        winSubTitle = ChatColor.translateAlternateColorCodes('&', config.getString("titles.win.subtitle", ""));
        looseTitle = ChatColor.translateAlternateColorCodes('&', config.getString("titles.loose.title", ""));
        looseSubTitle = ChatColor.translateAlternateColorCodes('&', config.getString("titles.loose.subtitle", ""));
        initiallizePeriod();
    }

    private void initiallizePeriod() {
        // 设置计时器时间
        int defWaitPeriod = plugin.getConfig().getInt("engine.default-timer.wait");
        int defGamePeriod = plugin.getConfig().getInt("engine.default-timer.game");
        int defFinishPeriod = plugin.getConfig().getInt("engine.default-timer.finish");
        int cfgWaitPeriod = config.getInt("period.wait");
        int cfgGamePeriod = config.getInt("period.game");
        int cfgFinishPeriod = config.getInt("period.finish");

        if (cfgWaitPeriod > 0)
            waitPeriod = cfgWaitPeriod;
        else if (defWaitPeriod > 0)
            waitPeriod = defWaitPeriod;

        if (cfgGamePeriod > 0)
            gamePeriod = cfgGamePeriod;
        else if (defGamePeriod > 0)
            gamePeriod = defGamePeriod;

        if (cfgFinishPeriod > 0)
            finishPeriod = cfgFinishPeriod;
        else if (defFinishPeriod > 0)
            finishPeriod = defFinishPeriod;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public Map<String, Location> getSpawn() {
        return spawn;
    }

    private Location getLocation(ConfigurationSection section) {
        return Utils.loadLocation(section);
    }
}
