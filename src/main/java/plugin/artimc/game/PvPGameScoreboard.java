package plugin.artimc.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import plugin.artimc.engine.GameStatus;
import plugin.artimc.engine.Party;
import plugin.artimc.scoreboard.BaseScoreboard;
import plugin.artimc.scoreboard.GameScoreboard;

public class PvPGameScoreboard extends GameScoreboard {

    public PvPGameScoreboard(PvPGame game) {
        super(game);
    }

    public PvPGameScoreboard(BaseScoreboard scoreboard, PvPGame game) {
        super(scoreboard, game);
    }

    protected PvPGame getGame() {
        return (PvPGame) game;
    }

    private String displayNumber(int n) {
        if (n < 10) return " " + n + " ";
        if (n < 100) return " " + n;
        return String.valueOf(n);
    }

    private String displayDouble(double n) {
        if (n < 1000.00) return String.format("%.2f", n) + " ";
        if (n < 10000.00) return String.format("%.2f", n / 1000) + "k";
        if (n < 100000.00) return String.format("%.2f", n / 10000) + "w";

        return ">10w ";
    }

    private String displayPlayerName(Player player) {

        String color = "&f";
        if (getGame().getHostParty().contains(player) && getGame().getHostParty().getPartyName() != null) {
            color = getGame().getHostParty().getPartyName().toString();
        } else if (getGame().getGuestParty().contains(player) && getGame().getGuestParty().getPartyName() != null) {
            color = getGame().getGuestParty().getPartyName().toString();
        }
        if (getGame().getGameStatus() == GameStatus.WAITING) {
            if (getGame().getHostParty().contains(player.getUniqueId())) {
                if (getGame().getHostParty().isOwner(player)) {
                    color = getGame().isHostReady() ? "&a✔ " : "  ";
                } else {
                    color = getGame().isHostReady() ? "&a  " : "  ";
                }
            } else if (getGame().getGuestParty().contains(player.getUniqueId())) {
                if (getGame().getGuestParty().isOwner(player)) {
                    color = getGame().isGuestReady() ? "&a✔ " : "  ";
                } else {
                    color = getGame().isGuestReady() ? "&a  " : "  ";
                }
            }
        }
        String name = player.getName();
        if (name.length() > 10) name = name.substring(0, 8);

        return color + name + " ".repeat(10 - name.length());
    }

    private String writePlayerData(Player player) {
        String temp = "  %player_name%  &6%damages%  &a%kill%   &e%assist%   &c%dead% ";
        return ChatColor.translateAlternateColorCodes('&', temp.replace("%player_name%", displayPlayerName(player)).replace("%damages%", displayDouble(getGame().getPvPStatstic() == null ? 0 : getGame().getPvPStatstic().getPlayerCausedDamage(player))).replace("%kill%", displayNumber(getGame().getPvPStatstic() == null ? 0 : getGame().getPvPStatstic().getPlayerKills(player))).replace("%assist%", displayNumber(getGame().getPvPStatstic() == null ? 0 : getGame().getPvPStatstic().getPlayerAssits(player))).replace("%dead%", displayNumber(getGame().getPvPStatstic() == null ? 0 : getGame().getPvPStatstic().getPlayerDeathes(player))));

    }

    List<String> getPartyMemberStatus(Party party) {
        List<String> list = new ArrayList<>();
        if (party != null) {
            for (Player p : party.getOnlinePlayers()) {
                list.add(writePlayerData(p));
            }
        }
        return list;
    }

    @Override
    protected String getTitle() {
        return getGame().getGameMap().getScoreboardTitle().replace("%name%", getGame().getGameName());
    }

    @Override
    protected List<String> getLines() {
        String hostPartyName = "主队 %party_custom_name%:";
        String guestPartynString = "客队 %party_custom_name%:";
        List<String> list = new ArrayList<>();
        list.add(" ");
        list.add(ChatColor.translateAlternateColorCodes('&', "             &6伤害  &a击杀  &e助攻  &4死亡    "));
        list.add(getGame().getHostParty().getPartyName().toString() + hostPartyName.replace("%party_custom_name%", getGame().getHostParty().getName()));
        list.addAll(getPartyMemberStatus(getGame().getHostParty()));
        list.add(" ");
        if (getGame().getGuestParty() != null) {
            list.add(ChatColor.translateAlternateColorCodes('&', "                     &f&lVS    "));
            list.add(getGame().getGuestParty().getPartyName().toString() + guestPartynString.replace("%party_custom_name%", getGame().getGuestParty().getName()));
            list.addAll(getPartyMemberStatus(getGame().getGuestParty()));
        }

        list.add(ChatColor.translateAlternateColorCodes('&', " "));
        return list;
    }

    @Override
    public void updateContent() {
        for (Player selectPlayer : game.getOnlinePlayers()) {
            Scoreboard sb = selectPlayer.getScoreboard();
            Party hostParty = getGame().getHostParty();
            Party guestparty = getGame().getGuestParty();
            if (hostParty != null && guestparty != null) {
                Team host = (sb.getTeam(hostParty.getPartyName().toString()) == null) ? sb.registerNewTeam(hostParty.getPartyName().toString()) : sb.getTeam(hostParty.getPartyName().toString());
                Team guest = (sb.getTeam(guestparty.getPartyName().toString()) == null) ? sb.registerNewTeam(guestparty.getPartyName().toString()) : sb.getTeam(guestparty.getPartyName().toString());
                for (Player ePlayer : game.getOnlinePlayers()) {
                    if (!(host.hasPlayer(ePlayer)) && (hostParty.contains(ePlayer.getUniqueId()))) {
                        host.addPlayer(ePlayer);
                    } else if (!(guest.hasPlayer(ePlayer)) && guestparty.contains(ePlayer.getUniqueId()))
                        guest.addPlayer(ePlayer);
                }
                host.setPrefix(hostParty.getPartyName().toString() + " " + hostParty.getName());
                guest.setPrefix(guestparty.getPartyName().toString() + " " + guestparty.getName());
            }
            super.updateContent();
        }
    }
}
