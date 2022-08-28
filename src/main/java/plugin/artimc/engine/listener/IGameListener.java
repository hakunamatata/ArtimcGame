package plugin.artimc.engine.listener;

import plugin.artimc.engine.event.GameItemPickupEvent;

import java.util.EventListener;

public interface IGameListener extends EventListener {

    void onGameItemPickup(GameItemPickupEvent event);
}
