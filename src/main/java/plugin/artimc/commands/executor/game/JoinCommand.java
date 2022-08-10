package plugin.artimc.commands.executor.game;

import java.util.List;

import org.bukkit.entity.Player;

import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Game;
import plugin.artimc.engine.GameMap;
import plugin.artimc.engine.GameStatus;
import plugin.artimc.game.PvPGame;

public class JoinCommand extends DefaultCommand {

    public JoinCommand(CommandContext context) {
        super(context);
    }

    @Override
    public List<String> suggest() {
        // game join #game
        if (!tryGetArg(1).isBlank()) {
            return List.of();
        }

        if (!tryGetArg(0).isBlank()) {
            return getPlugin().getConfigActiveGames();
        }

        return List.of();
    }

    @Override
    public boolean execute() {
        // /game join #game

        Player player = getPlayer();

        if (tryGetArg(1).isBlank()) throw new IllegalStateException(getLocaleString("error.missing-argument-game"));

        /**
         * Checkings
         */
        if (getManager().playerInGame(player))
            throw new IllegalStateException(getLocaleString("error.ur-already-in-game"));

        Game game = null;
        String gameName = tryGetArg(1);

        if (getManager().containesGame(gameName)) {
            game = getManager().getGame(gameName);
//            // 临时修一下
//            if (game.getOnlinePlayers().isEmpty()) {
//                game.close();
//                throw new IllegalStateException(getLocaleString("game.u-can-not-join"));
//            }
        } else {
            GameMap map = new GameMap(getPlugin().getGameConfigurations().get(gameName), getPlugin());
            switch (map.getType()) {
                default:
                    game = new PvPGame(gameName, getPlugin());
            }
        }

        if (game.getGameStatus() != GameStatus.WAITING)
            throw new IllegalStateException(getLocaleString("game.u-can-not-join"));

        game.addCompanion(player);
        return true;
    }
}
