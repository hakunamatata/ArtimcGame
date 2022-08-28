package plugin.artimc.commands.context;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import plugin.artimc.commands.executor.CommandExecutor;
import plugin.artimc.commands.executor.admin.*;

public class AdminCommandContext extends CommandContext {

    public AdminCommandContext(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args,
                               Plugin plugin) {
        super(sender, command, label, args, plugin);
    }

    @Override
    public ConfigurationSection getCommandConfiguration() {
        return this.getPlugin().getConfig().getConfigurationSection("commands.admin");
    }

    @Override
    public CommandExecutor getExecutor() {

        if (getArgs().length == 0)
            return new DefaultCommand(this);

        if (getArgs()[0].equals(this.getCommandConfiguration().getString("party")))
            return new PartyCommand(this);

        if (getArgs()[0].equals(this.getCommandConfiguration().getString("games")))
            return new GamesCommand(this);

        if (getArgs()[0].equals(this.getCommandConfiguration().getString("player")))
            return new PlayerCommand(this);

        return new DefaultCommand(this);
    }

    @Override
    public ConfigurationSection getParameterConfiguration() {
        return this.getPlugin().getConfig().getConfigurationSection("params.admin");
    }

}
