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

        if (getArgs()[0].equals(this.getCommandConfiguration().getString("join")))
            return new JoinCommand(this);

        if (getArgs()[0].equals(this.getCommandConfiguration().getString("ready")))
            return new ReadyCommand(this);

        if (getArgs()[0].equals(this.getCommandConfiguration().getString("unready")))
            return new UnreadyCommand(this);

        if (getArgs()[0].equals(this.getCommandConfiguration().getString("rule")))
            return new RuleCommand(this);

        if (getArgs()[0].equals(this.getCommandConfiguration().getString("move")))
            return new MoveCommand(this);

        if (getArgs()[0].equals(this.getCommandConfiguration().getString("set")))
            return new SetCommand(this);

        if (getArgs()[0].equals(this.getCommandConfiguration().getString("save")))
            return new SaveCommand(this);

        if (getArgs()[0].equals(this.getCommandConfiguration().getString("leave")))
            return new LeaveCommand(this);

        if (getArgs()[0].equals(this.getCommandConfiguration().getString("reload")))
            return new ReloadCommand(this);

        return new DefaultCommand(this);
    }

    @Override
    public ConfigurationSection getCommandConfiguration() {
        return this.getPlugin().getConfig().getConfigurationSection("commands.game");
    }

    @Override
    public ConfigurationSection getParameterConfiguration() {
        return this.getPlugin().getConfig().getConfigurationSection("params.game");
    }

}
