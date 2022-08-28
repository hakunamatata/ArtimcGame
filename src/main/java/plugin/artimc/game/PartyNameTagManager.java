package plugin.artimc.game;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import plugin.artimc.engine.NameTagManager;
import plugin.artimc.engine.Party;
import plugin.artimc.scoreboard.PlayerScoreboard;

public class PartyNameTagManager extends NameTagManager {

    public PartyNameTagManager(PvPGame game) {
        super(game);
    }

    private boolean availableGameParty(Party party) {
        return party != null && party.getPartyName() != null && party.getGame() != null;
    }

    @Override
    public PvPGame getGame() {
        return (PvPGame) super.getGame();
    }

    @Override
    protected void applyFor(Player identity) {
        Scoreboard board = identity.getScoreboard();
        Party hostParty = getGame().getHostParty();
        Party guestParty = getGame().getGuestParty();
        if (availableGameParty(hostParty) && availableGameParty(guestParty)) {
            Team host = board.getTeam(hostParty.getPartyName().toString());
            if (host == null) host = board.registerNewTeam(hostParty.getPartyName().toString());

            Team guest = board.getTeam(guestParty.getPartyName().toString());
            if (guest == null) guest = board.registerNewTeam(guestParty.getPartyName().toString());

            host.color(hostParty.getPartyName().getNamedTextColor());
            guest.color(guestParty.getPartyName().getNamedTextColor());
            for (Player player : getGame().getOnlinePlayers()) {
                if (!(host.hasPlayer(player)) && hostParty.contains(player)) {
                    host.addPlayer(player);
                } else if (!(guest.hasPlayer(player)) && guestParty.contains(player)) {
                    guest.addPlayer(player);
                }
            }
            host.prefix(Component.text(hostParty.getName() + " "));
            guest.prefix(Component.text(guestParty.getName() + " "));
        }

        super.applyFor(identity);
    }

    @Override
    protected void resetFor(Player identity) {
        Scoreboard board = identity.getScoreboard();
        Team[] teams = board.getTeams().toArray(new Team[0]);
        for (Team team : teams) {
            if (!team.getName().equals(PlayerScoreboard.DEFAULT_TRAM_NAME)) {
                board.getTeams().remove(team);
            }
        }
        super.resetFor(identity);
    }
}
