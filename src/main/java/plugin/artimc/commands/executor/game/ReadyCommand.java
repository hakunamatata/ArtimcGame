package plugin.artimc.commands.executor.game;

import java.util.List;

import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.game.PvPGame;

public class ReadyCommand extends DefaultCommand {

    public ReadyCommand(CommandContext context) {
        super(context);
    }

    @Override
    public List<String> suggest() {
        // game ready
        return List.of();
    }

    @Override
    public boolean execute() {
        /**
         * Checkings
         */
        if (getGame() == null)
            throw new IllegalStateException(getLocaleString("error.ur-not-in-game"));

        // 游戏内必须要队伍
        if (getParty() == null)
            throw new IllegalStateException(getLocaleString("command.ur-not-in-a-party"));

        // 必须是队长才能准备
        if(!getParty().isOwner(getPlayer()))
            throw new IllegalStateException(getLocaleString("command.ur-not-party-owner"));

        if (getGame() instanceof PvPGame) {
            PvPGame game = (PvPGame) getGame();
            game.setPartyReady(getParty(), true);
            return true;
        }

        throw new IllegalStateException(getLocaleString("game.no-need-to-ready"));
    }

}
