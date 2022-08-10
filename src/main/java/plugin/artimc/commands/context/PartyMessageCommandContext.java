package plugin.artimc.commands.context;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import plugin.artimc.commands.executor.CommandExecutor;
import plugin.artimc.commands.executor.partymessage.DefaultCommand;

public class PartyMessageCommandContext extends CommandContext {

    public PartyMessageCommandContext(@NotNull CommandSender sender, @NotNull Command command, String label,
            String[] args, Plugin plugin) {
        super(sender, command, label, args, plugin);
    }

    @Override
    protected ConfigurationSection getCommandCofniguration() {
        return null;
    }

    @Override
    public CommandExecutor getExecutor() {
        return new DefaultCommand(this);
    }

    @Override
    protected ConfigurationSection getParameterCofniguration() {
        return null;
    }

}
