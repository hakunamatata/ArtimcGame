package plugin.artimc.engine.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import plugin.artimc.engine.Game;
import plugin.artimc.engine.item.GameItem;

public class GameItemPickupEvent extends GameEvent {
    GameItem item;
    Player player;

    public GameItem getItem() {
        return item;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location;
    }

    Location location;

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    boolean cancel;

    public GameItemPickupEvent(GameItem item, Player player, Location location, Game game) {
        super(game);
        this.item = item;
        this.player = player;
        this.location = location;
        this.cancel = false;
    }
}
