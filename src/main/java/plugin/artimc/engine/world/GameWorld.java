package plugin.artimc.engine.world;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.scheduler.BukkitScheduler;
import plugin.artimc.ArtimcManager;
import plugin.artimc.ArtimcPlugin;

import java.io.File;

public class GameWorld {
    private final String name;
    MVWorldManager worldManager;
    WorldEdit worldEdit;
    private WorldStatus status;
    private ArtimcPlugin plugin;
    private BukkitScheduler scheduler;
    private World.Environment environment;
    private World world;

    public GameWorld(String worldName, World.Environment environment, ArtimcPlugin plugin) {
        this.name = worldName;
        this.plugin = plugin;
        this.environment = environment;
        initialize();
        this.world = plugin.getServer().getWorld(worldName);
    }

    public GameWorld(String worldName, ArtimcPlugin plugin) {
        this(worldName, World.Environment.NORMAL, plugin);
    }

    void initialize() {
        this.worldManager = plugin.getMultiverseCore().getMVWorldManager();
        this.worldEdit = plugin.getWorldEditPlugin().getWorldEdit();
        this.scheduler = plugin.getServer().getScheduler();
        createWorldIfAbsent();
        status = WorldStatus.INITIALIZED;
        if (exist()) status = WorldStatus.READY;
    }

    public World getWorld() {
        return world;
    }

    private void setWorld(World world) {
        this.world = world;
    }

    public WorldStatus getStatus() {
        return status;
    }

    public boolean isResetting() {
        return status.equals(WorldStatus.RESET);
    }

    public boolean isReady() {
        return status.equals(WorldStatus.READY);
    }

    private void setStatus(WorldStatus status) {
        this.status = status;
    }

    public ArtimcManager getManager() {
        return this.plugin.getManager();
    }

    /**
     * 当前世界是否存在
     *
     * @return
     */
    public boolean exist() {
        return Bukkit.getWorld(name) != null;
    }

    private void createWorldIfAbsent() {
        if (!exist()) worldManager.addWorld(name, environment, null, WorldType.NORMAL, false, "VoidGen");
    }

    public void reset(String schemaFile, int baseHeight) {
        if (!isResetting()) {
            setStatus(WorldStatus.RESET);
            worldManager.removePlayersFromWorld(name);
            reginWorld(schemaFile, baseHeight);
        }
    }

    private void reginWorld(String schemaFile, int baseHeight) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            scheduler.getMainThreadExecutor(plugin).execute(() -> {
                worldManager.regenWorld(name, false, false, null, true);
                plugin.getLogger().info(String.format("世界 %s 已重新生成 ", name));
                resetHologram();
                setTerrain(schemaFile, baseHeight);
            });
        });
    }

    private void setTerrain(String schemaFile, int baseHeight) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                File schema = new File(worldEdit.getSchematicsFolderPath() + File.separator + schemaFile);
                BukkitWorld world = new BukkitWorld(plugin.getServer().getWorld(name));
                ClipboardFormats.findByFile(schema).load(schema).paste(world, BlockVector3.at(0, baseHeight, 0), false, false, (Transform) null);
                setStatus(WorldStatus.READY);
                setWorld(plugin.getServer().getWorld(name));
                plugin.getLogger().info(String.format("%s 地形已经重置 ", name));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void resetHologram() {
        World world = plugin.getServer().getWorld(name);
        for (Hologram holo : HologramsAPI.getHolograms(plugin)) {
            if (holo.getWorld().equals(world)) holo.delete();
        }
    }
}
