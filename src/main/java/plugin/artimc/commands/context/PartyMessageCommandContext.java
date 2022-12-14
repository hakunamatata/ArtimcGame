package plugin.artimc.commands.context;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import plugin.artimc.commands.executor.CommandExecutor;
import plugin.artimc.commands.executor.partymessage.DefaultCommand;

public class PartyMessageCommandContext extends CommandContext {
    public PartyMessageCommandContext(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args, Plugin plugin) {
        super(sender, command, label, args, plugin);
    }


    @Override
    public CommandExecutor getExecutor() {
        return new DefaultCommand(this);
    }

    @Override
    protected ConfigurationSection getCommandConfiguration() {
        return this.getPlugin().getConfig().getConfigurationSection("commands.pm");
    }

    @Override
    public ConfigurationSection getParameterConfiguration() {
        return this.getPlugin().getConfig().getConfigurationSection("params.pm");
    }

}
