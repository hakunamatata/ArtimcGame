package plugin.artimc.common;

import org.bukkit.Server;
import plugin.artimc.*;

import java.util.logging.Logger;

public interface IComponent {

    ArtimcManager getManager();

    GameManager getGameManager();

    PlayerGameManager getPlayerGameManager();

    PlayerPartyManager getPlayerPartyManager();

    PlayerChannelManager getPlayerChannelManager();

    WorldManager getWorldManager();

    Server getServer();

    Logger getLogger();


}
