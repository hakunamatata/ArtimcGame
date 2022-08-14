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
import org.jetbrains.annotations.NotNull;
import plugin.artimc.engine.GameStatus;
import plugin.artimc.engine.Party;
import plugin.artimc.instance.LogFactoryGame;
import plugin.artimc.scoreboard.BaseScoreboard;
import plugin.artimc.scoreboard.GameScoreboard;

public class PvPGameScoreboard extends GameScoreboard {

    public PvPGameScoreboard(PvPGame game) {
        super(game);
    }

    public PvPGameScoreboard(BaseScoreboard scoreboard, PvPGame game) {
        super(scoreboard, game);
    }

    public PvPGameScoreboard(BaseScoreboard scoreboard, LogFactoryGame game) {
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

    private boolean playerInHost(Player player) {
        Party party = getGame().getHostParty();
        return party != null && party.contains(player);
    }

    private boolean playerInGuest(Player player) {
        Party party = getGame().getGuestParty();
        return party != null && party.contains(player);
    }

    private String displayPlayerName(Player player) {
        String color = "&f";
        Party party;
        // 玩家在主队
        if (playerInHost(player)) {
            party = getGame().getHostParty();
            // 游戏等待中....
            if (getGame().getGameStatus() == GameStatus.WAITING) {
                // 主队队长
                if (party.isOwner(player)) {
                    color = getGame().isHostReady() ? "&a✔★ " : "&e★  ";
                }
                // 主队成员
                else {
                    color = getGame().isHostReady() ? "&a●  " : "&e●  ";
                }
            }
            // 游戏其他状态
            else {
                // 名字颜色使用 队伍颜色
                color = party.getPartyName().toString();
            }

        } else if (playerInGuest(player)) {
            party = getGame().getGuestParty();
            // 游戏等待中....
            if (getGame().getGameStatus() == GameStatus.WAITING) {
                // 客队队长
                if (party.isOwner(player)) {
                    color = getGame().isGuestReady() ? "&a✔★ " : "&e★  ";
                }
                // 主队成员
                else {
                    color = getGame().isGuestReady() ? "&a●  " : "&e●  ";
                }
            }
            // 游戏其他状态
            else {
                // 名字颜色使用 队伍颜色
                color = party.getPartyName().toString();
            }
        }

        String name = player.getName();
        if (name.length() > 10) name = name.substring(0, 8);

        return color + name + " ".repeat(10 - name.length());
    }

    private @NotNull String writePlayerData(Player player) {
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
        List<String> list = new ArrayList<>();
        String hostPartyName = "主队 %party_custom_name%:";
        String guestPartynString = "客队 %party_custom_name%:";
        Party hostParty = getGame().getHostParty();
        Party guestParty = getGame().getGuestParty();

        if (hostParty != null) {
            list.add(" ");
            list.add(ChatColor.translateAlternateColorCodes('&', "             &6伤害  &a击杀  &e助攻  &4死亡    "));
            list.add(hostParty.getPartyName().toString() + hostPartyName.replace("%party_custom_name%", hostParty.getName()));
            list.addAll(getPartyMemberStatus(hostParty));
        }

        list.add(" ");

        if (guestParty != null) {
            list.add(ChatColor.translateAlternateColorCodes('&', "                     &f&lVS    "));
            list.add(guestParty.getPartyName().toString() + guestPartynString.replace("%party_custom_name%", guestParty.getName()));
            list.addAll(getPartyMemberStatus(guestParty));
        }

        // 如果有OB，显示OB名字
        if (getGame().getObserveParty().size() > 0) {
            list.add(" ");
            list.add(ChatColor.GRAY + "观察者：");
            String obNames = ChatColor.GRAY + "";
            for (Player player : getGame().getObserveParty().getOnlinePlayers())
                obNames += player.getName() + "; ";
            list.add(obNames);
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
                host.setColor(hostParty.getPartyName().getChatColor());
                guest.setColor(guestparty.getPartyName().getChatColor());
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
