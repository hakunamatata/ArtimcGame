package plugin.artimc;

import plugin.artimc.common.AbstractManager;
import plugin.artimc.engine.IGame;

import java.util.UUID;

public class PlayerGameManager extends AbstractManager<UUID, IGame> {
    public PlayerGameManager(ArtimcManager manager) {
        super(manager);
    }
}
