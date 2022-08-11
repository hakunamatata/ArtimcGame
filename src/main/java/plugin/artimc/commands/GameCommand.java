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
import plugin.artimc.commands.context.*;

public class GameCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;

    public GameCommand(Plugin plugin) {
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
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        try {
            CommandContext context = new GameCommandContext(sender, command, label, args, plugin);
            return context.getExecutor().suggest();
        } catch (IllegalStateException ex) {
            sender.sendMessage("§c" + ex.getMessage());
            return List.of();
        } catch (Exception e) {
            plugin.getServer().getLogger().warning(e.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        try {
            CommandContext context = new GameCommandContext(sender, command, label, args, plugin);
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
