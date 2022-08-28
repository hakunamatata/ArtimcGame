package plugin.artimc.commands.executor.partymessage;

import plugin.artimc.PlayerChannelManager;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.commands.executor.CommandExecutor;
import plugin.artimc.engine.Party;

import java.util.List;

public class DefaultCommand extends CommandExecutor {

    public DefaultCommand(CommandContext context) {
        super(context);
    }

    @Override
    public List<String> suggest() {
        return List.of();
    }

    @Override
    public boolean execute() {
        PlayerChannelManager mgr = getPlayerChannelManager();
        Party party = getPlayerPartyManager().get(getPlayer().getUniqueId());
        if (party == null) throw new IllegalStateException(getLocaleString("command.ur-not-in-a-party"));

        // /pm #message
        if (tryGetArg(0).isBlank()) {
            if (mgr.get(getPlayer().getUniqueId(), false)) {
                mgr.set(getPlayer().getUniqueId(), false);
                getPlayer().sendMessage(getLocaleString("command.disable-party-channel"));
            } else {
                mgr.set(getPlayer().getUniqueId(), true);
                getPlayer().sendMessage(getLocaleString("command.enable-party-channel"));
            }
        } else {
            String message = String.join(" ", getContext().getArgs());

            if (mgr.get(getPlayer().getUniqueId(), false)) {
                getPlayer().sendMessage(getLocaleString("command.enable-party-channel"));
                mgr.set(getPlayer().getUniqueId(), true);
            }
            party.chat(getPlayer(), message);
        }

        return true;
    }
}
