package plugin.artimc.commands.executor.party;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import net.kyori.adventure.text.Component;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Party;

public class ListCommand extends DefaultCommand {

    public ListCommand(CommandContext context) {
        super(context);
    }

    @Override
    public List<String> suggest() {
        // TODO Auto-generated method stub
        return Lists.newArrayList();
    }

    @Override
    public boolean execute() {
        Player player = getPlayer();
        Party party = getParty();

        if (party == null)
            throw new IllegalStateException(getLocaleString("command.ur-not-in-a-party"));

        List<UUID> players = new LinkedList<>(party.getPlayers());
        if (players.isEmpty()) {
            player.sendMessage(Component.text(getLocaleString("command.ur-not-in-a-party")));
            return false;
        }

        player.sendMessage(
                Component.text(getLocaleString("command.party-status-title").replace("%party_name%", party.getName())));

        for (UUID uuid : players) {
            OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
            String online = getLocaleString("online", false);
            String offline = getLocaleString("offline", false);
            String message = offlinePlayer.isOnline() ? online : offline;
            if (party.isOwner(offlinePlayer)) {
                player.sendMessage(Component.text(message + " §f§l" + offlinePlayer.getName()));
            } else
                player.sendMessage(Component.text(message + " §f" + offlinePlayer.getName()));
        }
        return true;
    }

}
