package plugin.artimc.commands.executor.game;

import java.util.List;

import org.bukkit.entity.Player;

import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Game;
import plugin.artimc.engine.GameMap;
import plugin.artimc.engine.GameStatus;
import plugin.artimc.engine.world.GameWorld;
import plugin.artimc.engine.world.WorldStatus;
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

        Player player = getPlayer();

        if (tryGetArg(1).isBlank()) throw new IllegalStateException(getLocaleString("error.missing-argument-game"));

        /**
         * Checkings
         */
        if (getManager().playerInGame(player))
            throw new IllegalStateException(getLocaleString("error.ur-already-in-game"));

        Game game = null;
        String gameName = tryGetArg(1);

        if (getGameManager().contains(gameName)) {
            game = (Game) getManager().getGameManager().get(gameName);
            if (game.getGameWorld().getStatus().equals(WorldStatus.RESET))
                throw new IllegalStateException(getLocaleString("game.world-is-on-reset"));
        } else {
            GameMap map = new GameMap(getPlugin().getGameConfigurations().get(gameName), getPlugin());
            GameWorld gameWorld;
            switch (map.getType()) {
                default:
                    gameWorld = getWorldManager().get(map.getWorldName());
                    // 游戏世界正在重置，不允许进入
                    if (gameWorld != null && gameWorld.getStatus().equals(WorldStatus.RESET))
                        throw new IllegalStateException(getLocaleString("game.world-is-on-reset"));

                    game = new LogFactoryGame(gameName, getPlugin());
            }
        }

        game.addCompanion(player);

        return true;
    }
}
