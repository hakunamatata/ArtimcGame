package plugin.artimc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import plugin.artimc.commands.AdminCommand;
import plugin.artimc.commands.GameCommand;
import plugin.artimc.commands.PartyCommand;
import plugin.artimc.commands.PartyMessageCommand;
import plugin.artimc.placeholder.GameManagerExpansion;

public final class ArtimcGamePlugin extends JavaPlugin {

    private ArtimcGameManager manager;
    private final Map<String, YamlConfiguration> gameConfigurations = new HashMap<>();
    private final Map<String, String> gameConfigPathes = new HashMap<>();
    private YamlConfiguration languageConfiguration;
    private MultiverseCore multiverseCore = null;
    private WorldEditPlugin worldEditPlugin = null;

    private HologramsAPI hologramsAPI = null;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        saveResource("games/test.yml", false);
        saveResource("language.yml", true);
        loadConfigurations();
        this.manager = new ArtimcGameManager(this);
        // 设置退出队伍聊天的指令
        this.getServer().getPluginManager().registerEvents(this.manager, this);
        this.getCommand("party").setExecutor(new PartyCommand(this));
        this.getCommand("game").setExecutor(new GameCommand(this));
        this.getCommand("partymessage").setExecutor(new PartyMessageCommand(this));
        this.getCommand("artimcadmin").setExecutor(new AdminCommand(this));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("============== Artimc Game Placeholder Enabled ==============");
            new GameManagerExpansion(this).register();
            getLogger().info("Artimc Game Placeholder Registered");
        }
        // Placeholder
        else {
            getLogger().info("============== Artimc Game Placeholder Disabled ==============");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")
                && Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
            multiverseCore = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
            worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
            getLogger().info("============== World Duplication Enabled ==============");
        } else {
            getLogger().info("============== World Duplication Disabled ==============");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            getLogger().info("============== Game Item Pickup Event Enabled ==============");
        } else {
            getLogger().info("============== Game Item Pickup Event Disabled ==============");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void reloadConfig() {
        gameConfigurations.clear();
        gameConfigPathes.clear();
        loadConfigurations();
        super.reloadConfig();
    }

    /**
     * 获取插件管理器
     *
     * @return
     */
    public ArtimcGameManager getManager() {
        return manager;
    }

    public MultiverseCore getMultiverseCore() {
        return multiverseCore;
    }

    public WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }

    /**
     * 获取所有的游戏配置
     *
     * @return
     */
    public Map<String, YamlConfiguration> getGameConfigurations() {
        return gameConfigurations;
    }

    /**
     * 获取配置文件中可以进行的游戏
     *
     * @return
     */
    public List<String> getConfigActiveGames() {
        return new ArrayList<>(gameConfigurations.keySet());
    }

    /**
     * 设置游戏配置
     *
     * @param gameName
     * @param path
     * @param value
     */
    public void setGameConfiguration(String gameName, String path, Object value) {
        if (gameConfigurations.get(gameName) != null) {
            gameConfigurations.get(gameName).set(path, value);
        }
    }

    /**
     * 将游戏配置保存至配置文件
     *
     * @param gameName
     */
    public void saveGameConfiguration(String gameName) {
        YamlConfiguration config = gameConfigurations.get(gameName);
        if (config != null) {
            try {
                config.save(gameConfigPathes.get(gameName));
            } catch (IOException e) {
                getLogger().info(e.getMessage());
            }
        }
    }

    public String getLocaleString(String path, boolean prefixed) {
        if (languageConfiguration != null && languageConfiguration.getString(path) != null) {
            String prefix = languageConfiguration.getString("prefix");
            String message = languageConfiguration.getString(path);
            if (prefixed)
                return ChatColor.translateAlternateColorCodes('&',
                        prefix + message);
            return ChatColor.translateAlternateColorCodes('&', message);
        }
        return "";
    }

    public String getLocaleString(String path) {
        return getLocaleString(path, true);
    }

    /**
     * 加载游戏目录中的所有游戏：/games/*.yml
     */
    private void loadConfigurations() {
        languageConfiguration = YamlConfiguration
                .loadConfiguration(new File(getDataFolder(), "language.yml"));
        File gamesFolder = new File(getDataFolder(), "games");
        if (gamesFolder.isDirectory()) {
            File[] gameFiles = gamesFolder.listFiles();
            YamlConfiguration gameYml;
            for (File f : gameFiles) {
                if (f.isFile() && f.getName().lastIndexOf(".yml") >= 0) {
                    gameYml = YamlConfiguration.loadConfiguration(f);
                    if (!gameYml.getString("name").isEmpty() && gameYml.getBoolean("active")) {
                        gameConfigurations.put(gameYml.getString("name"), gameYml);
                        gameConfigPathes.put(gameYml.getString("name"),
                                Path.of(gamesFolder.getPath(), f.getName()).toString());
                    }
                }
            }
        }
    }
}
