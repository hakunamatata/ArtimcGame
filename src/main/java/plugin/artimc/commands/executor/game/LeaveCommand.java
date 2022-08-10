package plugin.artimc.commands.executor.game;

import java.util.List;

import plugin.artimc.commands.context.CommandContext;

public class LeaveCommand extends DefaultCommand {

    public LeaveCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        // game leave
        if (getGame() == null) {
            getPlayer().sendMessage(getLocaleString("game.ur-not-in-game"));
        } else {
            getGame().leaveGame(getPlayer());
        }
        return false;
    }

    @Override
    public List<String> suggest() {
        // game leave
        return List.of();
    }

}
