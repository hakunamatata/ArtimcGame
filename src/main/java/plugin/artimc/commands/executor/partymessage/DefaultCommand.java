package plugin.artimc.commands.executor.partymessage;

import plugin.artimc.PlayerChannelManager;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.commands.executor.CommandExecutor;

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
        if (mgr.get(getPlayer().getUniqueId())) {
            mgr.set(getPlayer().getUniqueId(), false);
        } else {
            mgr.set(getPlayer().getUniqueId(), false);
        }
        return true;
    }
}
