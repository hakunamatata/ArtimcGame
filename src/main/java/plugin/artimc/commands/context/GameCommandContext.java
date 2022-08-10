package plugin.artimc.commands.context;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import plugin.artimc.commands.executor.CommandExecutor;
import plugin.artimc.commands.executor.game.*;

public class GameCommandContext extends CommandContext {

    public GameCommandContext(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args,
            Plugin plugin) {
        super(sender, command, label, args, plugin);
    }

    @Override
    public CommandExecutor getExecutor() {
        if (getArgs().length == 0)
            return new DefaultCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("join")))
            return new JoinCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("ready")))
            return new ReadyCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("unready")))
            return new UnreadyCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("rule")))
            return new RuleCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("set")))
            return new SetCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("save")))
            return new SaveCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("leave")))
            return new LeaveCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("reload")))
            return new ReloadCommand(this);

        return new DefaultCommand(this);
    }

    @Override
    public ConfigurationSection getCommandCofniguration() {
        return this.getPlugin().getConfig().getConfigurationSection("commands.game");
    }

    @Override
    public ConfigurationSection getParameterCofniguration() {
        return this.getPlugin().getConfig().getConfigurationSection("params.game");
    }

}
