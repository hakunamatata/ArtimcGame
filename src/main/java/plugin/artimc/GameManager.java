package plugin.artimc;

import plugin.artimc.common.AbstractManager;
import plugin.artimc.engine.IGame;

public class GameManager extends AbstractManager<String, IGame> {
    public GameManager(ArtimcManager manager) {
        super(manager);
    }
}
