package plugin.artimc.scoreboard;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;

import plugin.artimc.ArtimcManager;
import plugin.artimc.ArtimcPlugin;

public abstract class BaseScoreboard {

    private Map<UUID, String> names;

    protected Map<UUID, String> getNames() {
        return names;
    }

    protected Map<UUID, PlayerScoreboard> getScoreboards() {
        return scoreboards;
    }

    private Map<UUID, PlayerScoreboard> scoreboards;
    private boolean nameTagVisibility = true;
    private boolean collisionRule = true;
    private Plugin plugin;

    protected Plugin getPlugin() {
        return plugin;
    }

    protected BaseScoreboard(Plugin plugin) {
        this.plugin = plugin;
        this.names = new HashMap<>();
        this.scoreboards = new HashMap<>();
    }

    protected BaseScoreboard(Map<UUID, String> names, Map<UUID, PlayerScoreboard> scoreboards, Plugin plugin) {
        this.plugin = plugin;
        this.names = names;
        this.scoreboards = scoreboards;
    }

    protected ArtimcManager getManager() {
        return ((ArtimcPlugin) plugin).getManager();
    }

    /**
     * 将玩家添加到游戏计分板
     *
     * @param player 玩家
     * @param name   玩家名
     * @return 添加成功返回true
     */
    public boolean add(UUID player, String name) {
        if (scoreboards.containsKey(player))
            return false;
        PlayerScoreboard playerScoreboard = new PlayerScoreboard(plugin.getServer().getScoreboardManager());
        playerScoreboard.getTeam().addPlayer(Bukkit.getOfflinePlayer(player));
        scoreboards.put(player, playerScoreboard);
        names.put(player, name);
        setTeamOption(Team.Option.NAME_TAG_VISIBILITY, nameTagVisibility);
        setTeamOption(Team.Option.COLLISION_RULE, collisionRule);
        updateScoreboard(player);
        return true;
    }

    /**
     * 移除玩家的计分板
     *
     * @param player 玩家
     * @return 移除成功返回true
     */
    public boolean remove(UUID player) {
        if (!scoreboards.containsKey(player))
            return false;

        scoreboards.remove(player);
        String name = names.remove(player);
        for (PlayerScoreboard sb : scoreboards.values()) {
            sb.getTeam().removeEntry(name);
        }
        updateScoreboard(player);
        return true;
    }

    /**
     * 设置游戏内玩家命名牌是否可见
     *
     * @param value {@code true} if name tags should be visible
     */
    public void setNameTagVisibility(boolean value) {
        this.nameTagVisibility = value;
        setTeamOption(Team.Option.NAME_TAG_VISIBILITY, value);
    }

    /**
     * 设置游戏内玩家碰撞
     *
     * @param value {@code true} if players should be able to push other players
     */
    public void setCollisionRule(boolean value) {
        this.collisionRule = value;
        setTeamOption(Team.Option.COLLISION_RULE, value);
    }

    /**
     * Gets the player scoreboard for the given player, creating one if needed
     *
     * @param player Player
     * @return Player scoreboard
     */
    public PlayerScoreboard getPlayerScoreboard(UUID player) {
        return scoreboards.get(player);
    }

    /**
     * Gets all the player scoreboards for this game scoreboard
     *
     * @return Collection of player scoreboards
     */
    public Collection<PlayerScoreboard> getPlayerScoreboards() {
        return Collections.unmodifiableCollection(scoreboards.values());
    }

    protected abstract List<String> getLines();

    protected abstract String getTitle();

    public void updateContent() {
        String title = getTitle();
        List<String> lines = getLines();
        OfflinePlayer player;
        for (UUID uuid : scoreboards.keySet()) {
            updateScoreboard(uuid);
            player = getPlugin().getServer().getOfflinePlayer(uuid);
            PlayerScoreboard ps = getPlayerScoreboard(uuid);
            ps.setTitle(title == null ? "" : title);
            ps.setLines(lines == null ? List.of() : lines);

        }
    }

    /**
     * 为当前游戏设置队伍选项
     *
     * @param option
     * @param value
     */
    private void setTeamOption(Team.Option option, boolean value) {
        Team.OptionStatus status = value ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER;
        for (PlayerScoreboard sb : scoreboards.values()) {
            Team team = sb.getTeam();
            team.setOption(option, status);
            for (String name : names.values()) {
                team.addEntry(name);
            }
        }
    }

    private boolean updateScoreboard(UUID player) {
        Player p = plugin.getServer().getPlayer(player);
        if (p == null)
            return false;

        PlayerScoreboard scoreboard = scoreboards.get(player);
        if (scoreboard == null) {
            p.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
            return false;
        } else {
            p.setScoreboard(scoreboard.getScoreboard());
            return true;
        }
    }

//    protected String getPlayerTeamPrefix(OfflinePlayer player) {
//        return "";
//    }

}
