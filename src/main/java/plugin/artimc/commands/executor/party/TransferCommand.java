package plugin.artimc.commands.executor.party;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Party;

public class TransferCommand extends DefaultCommand {

    public TransferCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        Player player = getPlayer();
        Party party = getParty();

        if (tryGetArg(1).isBlank())
            throw new IllegalStateException(
                    getLocaleString("error.missing-argument-when-transfer"));

        if (party == null)
            throw new IllegalStateException(getLocaleString("command.ur-not-in-a-party"));

        if (!party.isOwner(player))
            throw new IllegalStateException(getLocaleString("command.ur-not-party-owner"));

        if (party.getOwnerName().equals(tryGetArg(1)))
            throw new IllegalStateException(getLocaleString("command.cant-trans-to-urself"));

        OfflinePlayer newOwner = getOfflinePlayer(tryGetArg(1));

        if (newOwner == null || !newOwner.isOnline())
            throw new IllegalStateException(
                    getLocaleString("player-offline").replace("%player_name%", newOwner.getName()));

        if (!party.contains(newOwner.getUniqueId()))
            throw new IllegalStateException(getLocaleString("command.only-trans-to-members"));

        if (party.transfer(newOwner)) {
            party.sendMessage(Component.text(
                    getLocaleString("command.player-promoted-as-owner").replace("%player_name%", newOwner.getName())));
        }

        return true;
    }

    @Override
    public List<String> suggest() {
        Party party = getParty();
        Player owner = null;
        List<String> memberNames = new ArrayList<>();
        if (party != null) {
            owner = party.getOwner();
            for (Player p : party.getOnlinePlayers())
                if (!p.equals(owner)) {
                    memberNames.add(p.getName());
                }
        }
        return memberNames;
    }

}
