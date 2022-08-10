package plugin.artimc.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import plugin.artimc.ArtimcGameManager;
import plugin.artimc.ArtimcGamePlugin;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.commands.context.PartyMessageCommandContext;

public class PartyMessageCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;

    public PartyMessageCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 游戏与队伍管理器
     * 
     * @return
     */
    protected ArtimcGameManager getManager() {
        return ((ArtimcGamePlugin) plugin).getManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        CommandContext context = new PartyMessageCommandContext(sender, command, label, args, plugin);
        return context.getExecutor().execute();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        CommandContext context = new PartyMessageCommandContext(sender, command, label, args, plugin);
        return context.getExecutor().suggest();
    }

}
