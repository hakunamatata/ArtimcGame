package plugin.artimc.commands.executor.party;

import org.bukkit.entity.Player;

import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Party;

public class LeaveCommand extends DefaultCommand {

    public LeaveCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        Player player = getPlayer();
        Party party = getParty();

        if (party == null)
            throw new IllegalStateException(getLocaleString("command.ur-not-in-a-party"));

        if (party.isOwner(player))
            throw new IllegalStateException(getLocaleString("command.owner-cant-leave"));

        if (party.leave(player.getUniqueId())) {
            party.sendMessage(getLocaleString("command.player-leave-party").replace("%player_name%", player.getName()));
            player.sendMessage(getLocaleString("command.u-have-left-party"));
            return true;
        }

        return super.execute();
    }

}
