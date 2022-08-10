package plugin.artimc.commands.executor.admin;

import net.md_5.bungee.api.ChatColor;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Game;

public class GamesCommand extends DefaultCommand {

    public GamesCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        for (Game g : getManager().getGames()) {
            String message = "&e游戏：%game% &a队伍：%parties% &7玩家：%players%"
                    .replace("%game%", g.getGameName())
                    .replace("%parties%", String.valueOf(g.getGameParties().size()))
                    .replace("%players%", String.valueOf(g.getPlayers().size()));
            getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
        return true;
    }
}
