package plugin.artimc.scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

import plugin.artimc.engine.Party;

/**
 * 描述：PartyScoreboard，计分板管理器
 * 管理队伍的计分板显示
 * 作者：Leo
 * 创建时间：2022/7/29 20:40
 */
public class PartyScoreboard extends BaseScoreboard {

    private static final String lineTemplate = " %online% %short_player_name% ";
    private Party party;

    public PartyScoreboard(Party party) {
        super(party.getPlugin());
        this.party = party;
    }

    public PartyScoreboard(BaseScoreboard scoreboard, Party party) {
        super(scoreboard.getNames(), scoreboard.getScoreboards(), party.getPlugin());
        this.party = party;
    }

    private String getPlayerStatus(UUID p) {
        OfflinePlayer player = getPlugin().getServer().getOfflinePlayer(p);
        String onlineText = player.isOnline() ? "§a●" : "§c○";
        if (party.isOwner(player)) {
            onlineText = player.isOnline() ? "§a★" : "§c☆";
        }
        return lineTemplate
                .replace("%short_player_name%", player.getName())
                .replace("%online%", onlineText);
    }

    @Override
    protected String getTitle() {
        return party.getName();
    }

    @Override
    public List<String> getLines() {
        List<String> lines = new ArrayList<>();
        for (UUID uuid : party.getPlayers()) {
            lines.add(getPlayerStatus(uuid));
        }
        return lines;
    }

}
