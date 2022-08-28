package plugin.artimc.engine.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import plugin.artimc.engine.Game;

import java.util.UUID;

public class PlayerLeaveGameEvent extends GameEvent {
    private final UUID player;

    public PlayerLeaveGameEvent(Game game, UUID player) {
        super(game);
        this.player = player;
    }

    public OfflinePlayer getPlayer() {
        return getGame().getServer().getOfflinePlayer(player);
    }
}
