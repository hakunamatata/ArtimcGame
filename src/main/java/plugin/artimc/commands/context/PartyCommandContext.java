package plugin.artimc.commands.context;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import plugin.artimc.commands.executor.CommandExecutor;
import plugin.artimc.commands.executor.party.*;

public class PartyCommandContext extends CommandContext {

    public PartyCommandContext(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args,
            Plugin plugin) {
        super(sender, command, label, args, plugin);
    }

    @Override
    public CommandExecutor getExecutor() {
        if (getArgs().length == 0)
            return new DefaultCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("create")))
            return new CreateCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("list")))
            return new ListCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("join")))
            return new JoinCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("invite")))
            return new InviteCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("dismiss")))
            return new DissmissCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("kick")))
            return new KickCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("transfer")))
            return new TransferCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("leave")))
            return new LeaveCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("set")))
            return new SetCommand(this);

        if (getArgs()[0].equals(this.getCommandCofniguration().getString("rename")))
            return new RenameCommand(this);

        return new DefaultCommand(this);
    }

    @Override
    public ConfigurationSection getCommandCofniguration() {
        return this.getPlugin().getConfig().getConfigurationSection("commands.party");
    }

    @Override
    public ConfigurationSection getParameterCofniguration() {
        return this.getPlugin().getConfig().getConfigurationSection("params.party");
    }

}
