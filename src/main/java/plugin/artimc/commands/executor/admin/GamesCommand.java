package plugin.artimc.commands.executor.admin;

import net.md_5.bungee.api.ChatColor;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Game;
import plugin.artimc.engine.IGame;

public class GamesCommand extends DefaultCommand {

    public GamesCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        for (IGame ig : getPlayerGameManager().list()) {
            if (ig instanceof Game) {
                Game game = (Game) ig;
                String message = "&e游戏：%game% &a队伍：%parties% &7玩家：%players%"
                        .replace("%game%", game.getGameName())
                        .replace("%parties%", String.valueOf(game.getGameParties().size()))
                        .replace("%players%", String.valueOf(game.getPlayers().size()));
                getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
        return true;
    }
}
