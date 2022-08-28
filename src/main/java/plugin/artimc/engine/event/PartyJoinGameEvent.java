package plugin.artimc.engine.event;

import org.bukkit.entity.Player;
import plugin.artimc.engine.Game;
import plugin.artimc.engine.Party;

public class PartyJoinGameEvent extends GameEvent {
    private final Party party;

    public PartyJoinGameEvent(Game game, Party party) {
        super(game);
        this.party = party;
    }

    public Party getParty() {
        return party;
    }

}
