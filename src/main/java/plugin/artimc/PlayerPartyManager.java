package plugin.artimc;

import plugin.artimc.common.AbstractManager;
import plugin.artimc.engine.Party;

import java.util.UUID;

public class PlayerPartyManager extends AbstractManager<UUID, Party> {

    public PlayerPartyManager(ArtimcManager manager) {
        super(manager);
    }

}
