package plugin.artimc.commands.executor.party;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Party;

public class KickCommand extends DefaultCommand {

    public KickCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        Player player = getPlayer();
        Party party = getParty();

        if (tryGetArg(1).isBlank())
            throw new IllegalStateException(
                    getLocaleString("error.missing-argument-when-kick"));

        if (party == null)
            throw new IllegalStateException(getLocaleString("command.ur-not-in-a-party"));

        if (!party.isOwner(player))
            throw new IllegalStateException(getLocaleString("command.ur-not-party-owner"));

        OfflinePlayer kickee = getPlugin().getServer().getOfflinePlayer(tryGetArg(1));
        party.leave(kickee.getUniqueId());
        party.sendMessage(Component.text(getLocaleString("command.party-kicked-player")
                .replace("%player_name%", kickee.getName())));

        ((CommandSender) kickee).sendMessage(Component.text(getLocaleString("command.ur-kicked-from-party")));
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
