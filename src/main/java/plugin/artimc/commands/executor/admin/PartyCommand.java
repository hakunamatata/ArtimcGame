package plugin.artimc.commands.executor.admin;

import net.md_5.bungee.api.ChatColor;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Party;

public class PartyCommand extends DefaultCommand {

    public PartyCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        for (Party p : getManager().getParties()) {
            int onlineSize = p.getOnlinePlayers().size();
            String message = "&e队长：%owner%  &a在线：%online%  &7:离线%offline%  "
                    .replace("%owner%", p.getOwnerName())
                    .replace("%online%", String.valueOf(p.getOnlinePlayers().size()))
                    .replace("%offline%", String.valueOf(p.size() - onlineSize));
            message += "&b当前游戏：" + p.getGame().getGameName();
            getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
        return true;
    }
}
