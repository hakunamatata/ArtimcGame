package plugin.artimc.common;

import org.bukkit.Server;
import org.bukkit.scheduler.BukkitScheduler;
import plugin.artimc.*;

import java.util.logging.Logger;

public class AbstractComponent implements IComponent {
    private final ArtimcPlugin plugin;

    public AbstractComponent(ArtimcPlugin plugin) {
        this.plugin = plugin;
    }

    public ArtimcPlugin getPlugin() {
        return plugin;
    }

    @Override
    public ArtimcManager getManager() {
        return plugin.getManager();
    }

    @Override
    public GameManager getGameManager() {
        return getManager().getGameManager();
    }

    @Override
    public PlayerGameManager getPlayerGameManager() {
        return getManager().getPlayerGameManager();
    }

    @Override
    public PlayerPartyManager getPlayerPartyManager() {
        return getManager().getPlayerPartyManager();
    }

    @Override
    public PlayerChannelManager getPlayerChannelManager() {
        return getManager().getPlayerChannelManager();
    }

    @Override
    public WorldManager getWorldManager() {
        return getManager().getWorldManager();
    }

    @Override
    public Server getServer() {
        return plugin.getServer();
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    public BukkitScheduler getScheduler() {
        return getServer().getScheduler();
    }
}
