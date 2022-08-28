package plugin.artimc.commands.executor;

import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import plugin.artimc.ArtimcManager;
import plugin.artimc.ArtimcPlugin;
import plugin.artimc.WorldManager;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.common.AbstractComponent;
import plugin.artimc.engine.IGame;
import plugin.artimc.engine.Party;

public abstract class CommandExecutor extends AbstractComponent {
    private CommandContext context;

    public CommandContext getContext() {
        return context;
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

    protected IGame getGame() {
        return getManager().getPlayerGame(getPlayer().getUniqueId());
    }

    protected CommandExecutor(CommandContext context) {
        super(context.getPlugin());
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
