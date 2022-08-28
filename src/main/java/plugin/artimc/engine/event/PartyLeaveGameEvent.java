package plugin.artimc.engine.event;

import plugin.artimc.engine.Game;
import plugin.artimc.engine.Party;

public class PartyLeaveGameEvent extends GameEvent {

    private final Party party;

    public PartyLeaveGameEvent(Game game, Party party) {
        super(game);
        this.party = party;
    }

    public Party getParty() {
        return party;
    }
}
