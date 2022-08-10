package plugin.artimc.commands.executor;

import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import plugin.artimc.ArtimcGameManager;
import plugin.artimc.ArtimcGamePlugin;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Game;
import plugin.artimc.engine.Party;

public abstract class CommandExecutor {

    private CommandContext context;

    public CommandContext getContext() {
        return context;
    }

    /**
     * 游戏与队伍管理器
     * 
     * @return
     */
    protected ArtimcGameManager getManager() {
        return context.getPlugin().getManager();
    }

    protected ArtimcGamePlugin getPlugin() {
        return context.getPlugin();
    }

    protected String tryGetArg(int index) {
        try {
            return getContext().getArgs()[index];
        } catch (Exception ex) {
            return "";
        }
    }

    protected String getLocaleString(String path) {
        return getLocaleString(path, true);
    }

    protected String getLocaleString(String path, boolean prefixed) {
        return getContext().getPlugin().getLocaleString(path, prefixed);
    }

    protected Player getPlayer() {
        if (!(getContext().getSender() instanceof Player)) {
            throw new IllegalStateException(getLocaleString("command.sender-is-not-player"));
        }
        return (Player) getContext().getSender();
    }

    protected boolean hasPermission(String perm) {
        return getPlayer().hasPermission(perm);
    }

    protected Party getParty() {
        return getManager().getPlayerParty(getPlayer().getUniqueId());
    }

    protected Game getGame() {
        return getManager().getPlayerGame(getPlayer().getUniqueId());
    }

    protected CommandExecutor(CommandContext context) {
        this.context = context;
    }

    protected OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer player = getPlugin().getServer().getPlayerExact(name); // find online player with given
                                                                             // name
        if (player == null) { // if offline, search usercache for player with given name
            for (OfflinePlayer p : getPlugin().getServer().getOfflinePlayers()) {
                if (name.equalsIgnoreCase(p.getName())) {
                    player = p;
                    break;
                }
            }
        }
        return player;
    }

    protected OfflinePlayer getOfflinePlayer(UUID uuid) {
        OfflinePlayer player = getPlugin().getServer().getOfflinePlayer(uuid);

        if (player == null) { // if offline, search usercache for player with given name
            for (OfflinePlayer p : getPlugin().getServer().getOfflinePlayers()) {
                if (uuid.equals(p.getUniqueId())) {
                    player = p;
                    break;
                }
            }
        }
        return player;
    }

    public abstract List<String> suggest();

    public abstract boolean execute();
}
