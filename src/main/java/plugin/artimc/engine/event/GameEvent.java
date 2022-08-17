package plugin.artimc.engine.event;

import plugin.artimc.engine.Game;

public class GameEvent {

    public Game getGame() {
        return game;
    }

    Game game;

    public GameEvent(Game game) {
        this.game = game;
    }


}
