package plugin.artimc.commands.executor.game;

import java.util.List;

import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.game.PvPGame;

public class UnreadyCommand extends DefaultCommand {

    public UnreadyCommand(CommandContext context) {
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

        if (getGame() instanceof PvPGame) {
            PvPGame game = (PvPGame) getGame();
            game.setPartyReady(getParty(), false);
            return true;
        }

        throw new IllegalStateException(getLocaleString("game.no-need-to-ready"));
    }
}
