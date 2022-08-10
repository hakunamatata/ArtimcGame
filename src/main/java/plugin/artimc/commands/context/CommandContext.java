package plugin.artimc.commands.context;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import plugin.artimc.ArtimcGameManager;
import plugin.artimc.ArtimcGamePlugin;
import plugin.artimc.commands.executor.CommandExecutor;

public abstract class CommandContext {

    private CommandSender sender;
    private Command bukkitCommand;
    private String label;
    private String[] args;

    public String[] getArgs() {
        return args;
    }

    private Plugin plugin;

    public ArtimcGamePlugin getPlugin() {
        return (ArtimcGamePlugin) plugin;
    }

    /**
     * 游戏与队伍管理器
     * 
     * @return
     */
    protected ArtimcGameManager getManager() {
        return ((ArtimcGamePlugin) plugin).getManager();
    }

    protected abstract ConfigurationSection getCommandCofniguration();

    protected abstract ConfigurationSection getParameterCofniguration();

    protected CommandContext(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args,
            Plugin plugin) {
        this.label = label;
        this.sender = sender;
        this.bukkitCommand = command;
        this.plugin = plugin;
        this.args = args;
    }

    public abstract CommandExecutor getExecutor();

    public String getCommand() {
        return this.bukkitCommand.getName();
    }

    public CommandSender getSender() {
        return sender;
    }

    public String getLabel() {
        return label;
    }
}
