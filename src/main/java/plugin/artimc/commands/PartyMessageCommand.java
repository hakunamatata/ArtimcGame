package plugin.artimc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.commands.context.PartyMessageCommandContext;

import java.util.List;

public class PartyMessageCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;

    public PartyMessageCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CommandContext context = new PartyMessageCommandContext(sender, command, label, args, plugin);
        return context.getExecutor().suggest();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            CommandContext context = new PartyMessageCommandContext(sender, command, label, args, plugin);
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
