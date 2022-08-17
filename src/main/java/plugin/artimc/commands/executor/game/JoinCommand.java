package plugin.artimc.commands.executor.game;

import java.util.List;

import org.bukkit.entity.Player;

import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Game;
import plugin.artimc.engine.GameMap;
import plugin.artimc.engine.GameStatus;
import plugin.artimc.game.PvPGame;
import plugin.artimc.instance.LogFactoryGame;

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

        } else {
            GameMap map = new GameMap(getPlugin().getGameConfigurations().get(gameName), getPlugin());
            switch (map.getType()) {
                default:
                    game = new LogFactoryGame(gameName, getPlugin());
            }
        }
        // 游戏准备阶段加入游戏
        if (game.getGameStatus() == GameStatus.WAITING)
            game.addCompanion(player);
            // 游戏开始之后，加入观察者
        else
            game.addObserver(player);

        return true;
    }
}
