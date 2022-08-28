package plugin.artimc.engine.event;

import org.bukkit.entity.Player;
import plugin.artimc.engine.Game;
import plugin.artimc.engine.Party;

public class PlayerJoinGameEvent extends GameEvent {

    private final Player player;
    private final boolean isObserver;

    public PlayerJoinGameEvent(Game game, Player player, boolean isObserver) {
        super(game);
        this.player = player;
        this.isObserver = isObserver;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isObserver() {
        return isObserver;
    }

    public Party getParty() {
        return game.getPlugin().getManager().getPlayerParty(player);
    }
}
