package plugin.artimc.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import plugin.artimc.ArtimcManager;
import plugin.artimc.ArtimcPlugin;
import plugin.artimc.commands.context.*;

public class PartyCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;

    public PartyCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 游戏与队伍管理器
     * 
     * @return
     */
    protected ArtimcManager getManager() {
        return ((ArtimcPlugin) plugin).getManager();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        CommandContext context = new PartyCommandContext(sender, command, label, args, plugin);
        return context.getExecutor().suggest();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        try {
            CommandContext context = new PartyCommandContext(sender, command, label, args, plugin);
            return context.getExecutor().execute();
        } catch (IllegalStateException ex) {
            sender.sendMessage("§c" + ex.getMessage());
            return false;
        } catch (Exception e) {
            plugin.getServer().getLogger().warning(e.getMessage());
            return false;
        }
    }

}
