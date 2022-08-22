package plugin.artimc.engine;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldType;

import java.io.File;

public class GameWorld {

    MVWorldManager worldManager;
    WorldEditPlugin worldEditPlugin;
    MultiverseCore core;
    WorldEdit worldEdit;
    Game game;

    public GameWorld(Game game) {
        this.game = game;
        this.core = game.getPlugin().getMultiverseCore();
        worldManager = this.core.getMVWorldManager();
        worldEditPlugin = game.getPlugin().getWorldEditPlugin();
        worldEdit = worldEditPlugin.getWorldEdit();
    }

    /**
     * 当前游戏的世界是否存在
     *
     * @return个
     *
     */
    public boolean exist() {
        String worldName = game.getGameMap().getWorldName();
        if (worldName.isEmpty()) throw new IllegalStateException("missing world name in game config.");
        return Bukkit.getWorld(worldName) != null;
    }

    /**
     * 重置这个世界
     *
     * @return
     */
    public void reset() {
        String worldName = game.getGameMap().getWorldName();
        File schema = new File(worldEdit.getSchematicsFolderPath() + File.separator + game.getGameMap().getSchemaFile());
        int height = game.getGameMap().getBaseHeight();
        // 将玩家从这个世界移除
        this.worldManager.removePlayersFromWorld(worldName);
        // 重新生成世界
        this.worldManager.regenWorld(worldName, false, false, null, true);

        // 将地形复制过来，异步执行
        Bukkit.getScheduler().runTaskAsynchronously(game.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    ClipboardFormats.findByFile(schema).load(schema).paste(createWorldIfAbsent(), BlockVector3.at(0, height, 0), false, false, (Transform) null);
                    game.log(String.format("地形已重置"));
                    game.getPlugin().getServer().getScheduler().getMainThreadExecutor(game.getPlugin()).execute(() -> {
                        game.onWorldReset();
                    });
                    // 需要添加时间通知
                    // 仍有多次导入地形的情况发生
                } catch (Exception ex) {
                    game.getPlugin().getLogger().warning(ex.getMessage());
                }
            }
        });
    }

    /**
     * 获取游戏的世界，如果没有的话，为他创建一
     * @return
     */
    public BukkitWorld createWorldIfAbsent() {
        String worldName = game.getGameMap().getWorldName();
        World existWorld = Bukkit.getWorld(worldName);
        World.Environment env = game.getGameMap().getEnvironment();
        if (existWorld == null) {
            this.worldManager.addWorld(worldName, env, null, WorldType.NORMAL, false, "VoidGen");
            existWorld = Bukkit.getWorld(worldName);
        }
        for (Hologram holo : HologramsAPI.getHolograms(game.getPlugin())) {
            if (holo.getWorld().equals(existWorld))
                holo.delete();
        }
        return new BukkitWorld(existWorld);
    }
}
