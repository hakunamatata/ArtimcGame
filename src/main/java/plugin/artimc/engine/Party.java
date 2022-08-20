package plugin.artimc.engine;

import java.util.*;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.Component;
import plugin.artimc.ArtimcGameManager;
import plugin.artimc.ArtimcGamePlugin;
import plugin.artimc.scoreboard.BaseScoreboard;
import plugin.artimc.scoreboard.PartyScoreboard;

/**
 * 描述：BaseParty，队伍管理器
 * 管理游戏中的队伍
 * 作者：Leo
 * 创建时间：2022/7/29 20:40
 */
public class Party {

    private Plugin plugin;
    private final Set<UUID> players;
    private final Set<UUID> invitees;
    private BaseScoreboard scoreboard;
    private boolean friendlyFire = true;
    private Game game;
    private String customName = "";
    private PartyName partyName;
    private UUID owner;

    public Party(Plugin plugin) {
        this.plugin = plugin;
        this.players = new HashSet<>();
        this.invitees = new HashSet<>();
        this.scoreboard = new PartyScoreboard(this);
        onCreated();
    }

    public Party(Player player, Plugin plugin) {
        this(plugin);
        this.owner = player.getUniqueId();
        join(player);
    }

    public Party(Game game, Plugin plugin) {
        this(plugin);
        this.game = game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setPartyName(PartyName partyName) {
        this.partyName = partyName;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * 获取队伍当前所在的游戏
     *
     * @return
     */
    public Game getGame() {
        return game;
    }

    /**
     * 获取队伍的自定义名称
     *
     * @return
     */
    public String getCustomName() {
        return customName;
    }

    /**
     * 设置队伍的自定义名称
     *
     * @param customName
     */
    public void setCustomName(String customName) {
        this.customName = customName;
    }

    /**
     * 获取队伍的PartyName
     *
     * @return
     */
    public PartyName getPartyName() {
        return partyName;
    }

    /**
     * 自动获取队伍名称，优先级：
     * 1. 自定义名称
     * 2. PartyName名称
     * 3. 队长名称
     */
    public String getName() {

        String color = "§f";
        if (customName != null && !customName.isBlank()) {
            if (game != null && partyName != null) {
                return partyName + "§o" + customName + "§r";
            } else {
                return color + "§o" + customName + "§r";
            }
        }

        if (game != null && partyName != null) {
            color = partyName.toString();
            switch (partyName) {
                case ORANGE:
                    return color + "橙队";
                case YELLOW:
                    return color + "黄队";
                case GREEN:
                    return color + "绿队";
                case LIME:
                    return color + "青队";
                case BLUE:
                    return color + "蓝队";
                case PURPLE:
                    return color + "紫队";
                default:
                    return color + "红队";
            }
        }

        if (owner != null) {
            OfflinePlayer player = getOfflinePlayer(owner);
            return color + player.getName() + getLocaleString("command.s-party", false);
        }

        return getLocaleString("default-party-name", false);
    }

    protected ArtimcGameManager getManager() {
        return ((ArtimcGamePlugin) plugin).getManager();
    }

    public String getLocaleString(String path) {
        return getLocaleString(path, true);
    }

    public String getLocaleString(String path, boolean prefix) {
        ArtimcGamePlugin p = (ArtimcGamePlugin) plugin;
        return p.getLocaleString(path, prefix);
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    /**
     * 获取队伍里的玩家
     *
     * @return 玩家Set
     */
    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    /**
     * 获取队伍的计分板
     *
     * @return
     */
    public BaseScoreboard getScoreboard() {
        return scoreboard;
    }

    protected void setScoreboard(BaseScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    /**
     * 获取队伍中所有的在线玩家
     *
     * @param players
     * @return
     */
    private Set<Player> getOnlinePlayers(Set<UUID> players) {
        HashSet<Player> onlinePlayers = new HashSet<>();
        for (UUID uuid : players) {
            Player player = getOnlinePlayer(uuid);
            if (player != null) onlinePlayers.add(player);
        }
        return Collections.unmodifiableSet(onlinePlayers);
    }

    public Set<Player> getOnlinePlayers() {
        return getOnlinePlayers(players);
    }

    /**
     * 获取队长玩家
     *
     * @return
     */
    public Player getOwner() {
        return getOnlinePlayer(owner);
    }

    public void setOwner(Player player) {
        owner = player.getUniqueId();
    }

    /**
     * 获取队长玩家的名字
     *
     * @return
     */
    public String getOwnerName() {
        OfflinePlayer player = getOfflinePlayer(owner);
        return player.getName();
    }

    /**
     * 玩家加入队伍
     *
     * @param player
     * @return
     * @throws IllegalStateException 如果玩家在其他队伍
     */
    protected boolean add(UUID player) throws IllegalStateException {

        if (!getManager().playerJoinParty(player, this))
            throw new IllegalStateException(getLocaleString("command.player-in-another-party"));
        invitees.remove(player);
        return players.add(player);
    }

    /**
     * 玩家加入队伍
     *
     * @param player
     * @return
     */
    public boolean join(Player player) {
        if (add(player.getUniqueId())) {
            scoreboard.add(player.getUniqueId(), player.getName());
            onPlayerJoinParty(player);
            return true;
        }
        return false;
    }

    /**
     * 玩家离开队伍
     *
     * @param player
     * @return
     */
    protected boolean remove(UUID player) {
        if (players.remove(player)) {
            if (!getManager().playerLeaveParty(player)) {
                plugin.getLogger().warning("player already leave team, but sync failed in manager.");
            }
            return true;
        }
        return false;
    }

    /**
     * 玩家离开队伍
     *
     * @param player
     * @return
     */
    public boolean leave(Player player) {
        if (remove(player.getUniqueId())) {
            scoreboard.remove(player.getUniqueId());
            onPlayerLeaveParty(player);
            return true;
        }
        return false;
    }

    /**
     * 玩家离开队伍
     *
     * @param player
     * @return
     */
    public boolean leave(OfflinePlayer player) {
        if (player.isOnline()) {
            return leave((Player) player);
        }
        return remove(player.getUniqueId());
    }

    /**
     * 检测玩家是否在队伍中
     *
     * @param player
     * @return
     */
    public boolean contains(UUID player) {
        return players.contains(player);
    }

    /**
     * 检测玩家是否在队伍中
     *
     * @param player
     * @return
     */
    public boolean contains(Player player) {
        return contains(player.getUniqueId());
    }

    /**
     * 获取队伍玩家数量
     *
     * @return
     */
    public int size() {
        return players.size();
    }

    /**
     * 队伍是否人数已满
     *
     * @return
     */
    public boolean willOverload() {
        int maxPlayer = getPlugin().getConfig().getInt("settings.max-player", 5);
        return size() >= maxPlayer;
    }

    /**
     * 检测玩家是否为队长
     *
     * @param player
     * @return
     */
    public boolean isOwner(Player player) {
        return player.getUniqueId().equals(owner);
    }

    public boolean isOwner(OfflinePlayer player) {
        return player.getUniqueId().equals(owner);
    }

    public boolean isOwner(UUID player) {
        return player.equals(owner);
    }

    /**
     * 将队长转移给队伍中的其他玩家
     *
     * @param player
     * @return
     * @throws IllegalStateException 如果玩家不咋这个队伍中
     */
    public boolean transfer(UUID player) throws IllegalStateException {
        if (!players.contains(player)) throw new IllegalStateException("你要转让的玩家不在队伍中");
        owner = player;
        return true;
    }

    /**
     * 将队长转移给队伍中的其他玩家
     *
     * @param player
     * @return
     */
    public boolean transfer(Player player) {
        onTransfering(player);
        if (transfer(player.getUniqueId())) {
            onTransferd(player);
            return true;
        }
        return false;
    }

    public boolean transfer(OfflinePlayer player) {
        if (player.isOnline()) {
            return transfer((Player) player);
        }
        return transfer(player.getUniqueId());
    }

    /**
     * 邀请玩家加入队伍
     *
     * @param player
     * @return
     * @throws IllegalStateException 如果玩家已经在队伍中
     */
    public boolean invite(UUID player) throws IllegalStateException {
        if (players.contains(player))
            throw new IllegalStateException(getLocaleString("command.player-already-in-party").replace("%player_name%", plugin.getServer().getPlayer(player).getName()));
        if (getManager().playerInParty(player))
            throw new IllegalStateException(getLocaleString("command.player-in-another-party").replace("%player_name%", plugin.getServer().getPlayer(player).getName()));
        return invitees.add(player);
    }

    /**
     * 邀请玩家加入队伍
     *
     * @param player
     * @return
     */
    public boolean invite(Player player) {
        if (invite(player.getUniqueId())) {
            onInviting(player);
            return true;
        }
        return false;
    }

    /**
     * 取消邀请
     *
     * @param player
     * @return
     * @throws IllegalStateException 如果玩家已经在队伍中
     */
    public boolean uninvite(UUID player) throws IllegalStateException {
        if (players.contains(player))
            throw new IllegalStateException(getLocaleString("command.player-already-in-party").replace("%player_name%", plugin.getServer().getPlayer(player).getName()));
        return invitees.remove(player);
    }

    /**
     * 取消邀请
     *
     * @param player
     * @return
     */
    public boolean uninvite(Player player) {
        return uninvite(player.getUniqueId());
    }

    /**
     * 检测玩家是否被邀请
     *
     * @param player
     * @return
     */
    public boolean isInvited(UUID player) {
        return invitees.contains(player);
    }

    public boolean isInvited(Player player) {
        return isInvited(player.getUniqueId());
    }

    /**
     * 解散队伍
     */
    public void dismiss() {
        Object[] uuids = getPlayers().toArray().clone();
        for (int i = 0; i < uuids.length; i++) {
            UUID uuid = (UUID) uuids[i];
            remove(uuid);
        }
    }

    public void sendMessage(Component component) {
        for (Player p : getOnlinePlayers(players)) {
            p.sendMessage(component);
        }
    }

    public void sendMessage(String message) {
        sendMessage(Component.text(ChatColor.translateAlternateColorCodes('&', message)));
    }

    /**
     * 队伍消息
     *
     * @param chat 消息内容
     */
    public void chat(Player player, String chat) {
        if (!this.contains(player) || chat.isBlank()) return;

        String format = ChatColor.translateAlternateColorCodes('&', getPlugin().getConfig().getString("chat.format"));
        String message = format.replace("%player_name%", player.getName()).replace("%message%", chat);
        sendMessage(message);
    }

    public void setSpawn(Location location) {
        for (Player player : getOnlinePlayers()) {
            player.setBedSpawnLocation(location);
        }
    }

    /**
     * 更新队伍中的计分板
     */
    public void updateScoreboard() {
        scoreboard.updateContent();
    }

    /**
     * 更新玩家的计分板
     * 此方法将同时更新队伍所有成员的计分板
     *
     * @param p
     */
    private void updatePlayerScoreboard(UUID p) {
        OfflinePlayer player = getPlugin().getServer().getOfflinePlayer(p);

        // 如果玩家在队伍中 且 玩家在线 且 玩家没有计分板，则显示玩家计分板
        if (contains(p) && player.isOnline() && scoreboard.getPlayerScoreboard(p) == null) {
            scoreboard.add(p, player.getName());
        }
        // 如果玩家不在队伍中 且 队伍计分板中包含这个玩家的计分板，则删除
        else if (!contains(p) && scoreboard.getPlayerScoreboard(p) != null) {
            scoreboard.remove(p);
        }

        // 更新队伍中的计分板
        updateScoreboard();
    }

    /**
     * 基于玩家事件，延迟0.5s更新队伍内的计分板
     * 如果队伍内没有玩家，将自动解散队伍
     *
     * @param player
     */
    private void updateScoreboardOnPlayerEvent(Player player) {
        final UUID uuid = player.getUniqueId();
        getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
            if (getOnlinePlayers().isEmpty()) dismiss();
            else updatePlayerScoreboard(uuid);
        }, 5);
    }

    /**
     * 静默移动玩家
     *
     * @param player
     */
    public void removeSilent(Player player) {
        players.remove(player.getUniqueId());
        getScoreboard().remove(player.getUniqueId());
    }

    /**
     * 静默添加
     *
     * @param player
     */
    public void addSilent(Player player) {
        players.add(player.getUniqueId());
        getScoreboard().add(player.getUniqueId(), player.getName());
    }

    /**
     * 获取在线玩家
     *
     * @param uuid
     * @return
     */
    private Player getOnlinePlayer(UUID uuid) {
        return plugin.getServer().getPlayer(uuid);
    }

    private OfflinePlayer getOfflinePlayer(UUID uuid) {
        return plugin.getServer().getOfflinePlayer(uuid);
    }

    /**
     * 将队伍成员传送至目标位置
     *
     * @param location
     */
    public void teleport(Location location) {
        for (Player player : getOnlinePlayers()) {
            player.teleport(location);
        }
    }

    public void showTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : getOnlinePlayers()) {
            player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
        }
    }

    public void showTitle(String title, String subTitle) {
        for (Player player : getOnlinePlayers()) {
            player.sendTitle(title, subTitle, 20, 60, 20);
        }
    }


    /************************************************************************************************************
     *
     *
     *
     * 事件处理
     *
     *
     *
     *************************************************************************************************************/

    /**
     * 当队伍创建时候执行
     */
    protected void onCreated() {
        updateScoreboard();
    }

    /**
     * 当玩家上线时执行
     * 玩家登录时，如果当前有队伍，更新队伍的计分板
     *
     * @param player
     */
    public void onPlayerJoin(Player player) {
        updateScoreboardOnPlayerEvent(player);
    }

    /**
     * 当玩家离线时执行
     * 玩家退出时，如果当前有队伍，更新队伍计分板
     *
     * @param player
     */
    public void onPlayerQuit(Player player) {
        // 离线，将队长交给其他成员
        if (isOwner(player)) {
            for (Player p : getOnlinePlayers()) {
                if (!player.equals(p)) {
                    setOwner(p);
                    sendMessage(Component.text(getLocaleString("command.player-promoted-as-owner").replace("%player_name%", p.getName())));
                    break;
                }
            }
        }
        updateScoreboardOnPlayerEvent(player);
    }

    /**
     * 当玩家离开队伍时执行
     *
     * @param player
     */
    protected void onPlayerJoinParty(Player player) {
        updateScoreboardOnPlayerEvent(player);
    }

    /**
     * 当玩家离开队伍时执行
     *
     * @param player
     */
    protected void onPlayerLeaveParty(Player player) {
        if (game != null) {
            game.leaveGame(player);
        }
        updateScoreboardOnPlayerEvent(player);
    }

    /**
     * 当队长邀请玩家时执行
     */
    protected void onInviting(Player player) {

    }

    /**
     * 当队伍被转让执行
     *
     * @param player
     */
    protected void onTransferd(Player player) {
        updateScoreboardOnPlayerEvent(player);
    }

    /**
     * 但队伍转让前执行
     *
     * @param player
     */
    protected void onTransfering(Player player) {

    }

    /**
     * 玩家聊天时触发
     *
     * @param event
     */
    public void onPlayerChat(PlayerChatEvent event) {
        if (getManager().isPlayerEnabledPartyChannel(event.getPlayer().getUniqueId())) {
            chat(event.getPlayer(), event.getMessage());
            event.setCancelled(true);
        }
    }

    /**
     * 当玩家被实体伤害的时候触发
     * 队伍误伤机制
     *
     * @param event
     */
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        Player damager = null;
        Player player = (Player) event.getEntity();
        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else {
            switch (event.getCause()) {
                case MAGIC:
                case PROJECTILE:
                    Projectile proj = (Projectile) event.getDamager();
                    if (proj.getShooter() instanceof Player) {
                        damager = (Player) proj.getShooter();
                    }
                    break;
                default:
                    break;
            }
        }

        if (damager instanceof Player) {
            if (this.contains(damager) && this.contains(player)) {
                if (!friendlyFire) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
