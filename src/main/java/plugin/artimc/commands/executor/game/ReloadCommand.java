package plugin.artimc.commands.executor.game;

import java.util.List;

import plugin.artimc.commands.context.CommandContext;

public class ReloadCommand extends DefaultCommand {

    public ReloadCommand(CommandContext context) {
        super(context);
    }

    @Override
    public List<String> suggest() {
        return List.of();
    }

    @Override
    public boolean execute() {
        getPlugin().reloadConfig();
        getPlayer().sendMessage(getLocaleString("command.reload-success", false));
        return true;
    }
}
