package plugin.artimc;

import plugin.artimc.common.AbstractManager;
import plugin.artimc.engine.world.GameWorld;

public class WorldManager extends AbstractManager<String, GameWorld> {
    public WorldManager(ArtimcManager manager) {
        super(manager);
    }
}
