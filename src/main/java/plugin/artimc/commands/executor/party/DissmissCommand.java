package plugin.artimc.commands.executor.party;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Party;

public class DissmissCommand extends DefaultCommand {

    public DissmissCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        Player player = getPlayer();
        Party party = getParty();

        if (party == null)
            throw new IllegalStateException(getLocaleString("command.ur-not-in-a-party"));

        if (!party.isOwner(player))
            throw new IllegalStateException(getLocaleString("command.ur-not-party-owner"));

        Object[] uuids = party.getPlayers().toArray().clone();
        for (int i = 0; i < uuids.length; i++) {
            UUID uuid = (UUID) uuids[i];
            Player removee = getPlugin().getServer().getPlayer(uuid);
            if (party.leave(removee)) {
                removee.sendMessage(Component.text(getLocaleString("command.party-dismissed")
                        .replace("%player_name%", player.getName())));
            }
        }

        return true;
    }

    @Override
    public List<String> suggest() {
        // TODO Auto-generated method stub
        return null;
    }

}
