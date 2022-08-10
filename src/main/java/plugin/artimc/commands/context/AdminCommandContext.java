package plugin.artimc.commands.context;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import plugin.artimc.commands.executor.CommandExecutor;
import plugin.artimc.commands.executor.admin.DefaultCommand;
import plugin.artimc.commands.executor.admin.GamesCommand;
import plugin.artimc.commands.executor.admin.PartyCommand;
import plugin.artimc.commands.executor.admin.PlayerCommand;

public class AdminCommandContext extends CommandContext {

    public AdminCommandContext(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args,
            Plugin plugin) {
        super(sender, command, label, args, plugin);
    }

    @Override
    public ConfigurationSection getCommandCofniguration() {
        return this.getPlugin().getConfig().getConfigurationSection("commands.admin");
    }

    @Override
    public CommandExecutor getExecutor() {

        if (getArgs().length == 0)
            return new DefaultCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("party")))
            return new PartyCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("games")))
            return new GamesCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("player")))
            return new PlayerCommand(this);

        return new DefaultCommand(this);
    }

    @Override
    public ConfigurationSection getParameterCofniguration() {
        return this.getPlugin().getConfig().getConfigurationSection("params.admin");
    }

}
