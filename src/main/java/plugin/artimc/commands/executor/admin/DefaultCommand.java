package plugin.artimc.commands.executor.admin;

import java.util.List;

import plugin.artimc.commands.context.AdminCommandContext;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.commands.executor.CommandExecutor;

public class DefaultCommand extends CommandExecutor {

    @Override
    public AdminCommandContext getContext() {
        return (AdminCommandContext) super.getContext();
    }

    public DefaultCommand(CommandContext context) {
        super(context);
    }

    protected String getCommandString(String path) {
        return getContext().getCommandCofniguration().getString(path);
    }

    protected String getParamsString(String path) {
        return getContext().getParameterCofniguration().getString(path);
    }

    @Override
    protected String getLocaleString(String path) {
        return getPlugin().getLocaleString(path, false);
    }

    @Override
    public List<String> suggest() {

        if (tryGetArg(0).isBlank())
            return List.of(
                    getCommandString("player"),
                    getCommandString("games"),
                    getCommandString("party"));
        return List.of();
    }

    @Override
    public boolean execute() {
        return true;
    }

}
