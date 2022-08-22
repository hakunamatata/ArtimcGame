package plugin.artimc;

import java.util.*;

import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;

import plugin.artimc.engine.Game;
import plugin.artimc.engine.Party;
import plugin.artimc.utils.Utils;

/**
 * 描述：GameManager，游戏管理器
 * 管理游戏中的玩家、队伍、对游戏事件进行分发
 * 游戏创造器，游戏工厂
 * 作者：Leo
 * 创建时间：2022/7/29 20:40
 */
public class ArtimcGameManager implements Listener {

    private final Plugin plugin;

    // 玩家所在队伍
    private final Map<UUID, Party> parties;

    // 玩家所在游戏
    private final Map<UUID, Game> games;

    //private final Map<String, >

    // 玩家队伍聊天频道
    private final Set<UUID> enablesPartyChannel;

    public ArtimcGameManager(Plugin plugin) {
        this.plugin = plugin;
        this.parties = new HashMap<>();
        this.games = new HashMap<>();
        this.enablesPartyChannel = new HashSet<>();

    }

    /**
     * 获取当前的插件
     *
     * @return 插件
     */
    public Plugin getPlugin() {
        return plugin;
    }

    public Player getOnlinePlayer(UUID player) {
        return getPlugin().getServer().getPlayer(player);
    }

    public Set<Party> getParties() {
        Set<Party> newSet = new HashSet<>();
        for (Party p : parties.values())
            if (!newSet.contains(p)) newSet.add(p);
        return newSet;
    }

    public Set<Game> getGames() {
        return Set.of(games.values().toArray(new Game[0]));
    }

    /**
     * 根据uuid，获取 Bukkit {@link Player 玩家} 示例
     *
     * @param uuids UUIDs
     * @return Set of Bukkit players
     */
    public Set<Player> getOnlinePlayers(Set<UUID> uuids) {
        HashSet<Player> onlinePlayers = new HashSet<>();
        for (UUID uuid : uuids) {
            Player player = getOnlinePlayer(uuid);
            if (player != null) onlinePlayers.add(player);
        }
        return Collections.unmodifiableSet(onlinePlayers);
    }

    /**
     * 检测玩家是否在任意一个队伍中
     *
     * @param player Player
     * @return {@code true} if the player is in a party
     */
    public boolean playerInParty(UUID player) {
        return parties.containsKey(player);
    }

    public boolean playerInParty(Player player) {
        return playerInParty(player.getUniqueId());
    }

    /**
     * 获取玩家所在的队伍
     *
     * @param player Player
     * @return Possibly null party
     */
    public Party getPlayerParty(UUID player) {
        return parties.get(player);
    }

    public Party getPlayerParty(Player player) {
        return getPlayerParty(player.getUniqueId());
    }

    /**
     * 检测玩家是否在任意一个游戏中
     *
     * @param player Player
     * @return {@code true} if the player is in a party
     */
    public boolean playerInGame(UUID player) {
        return games.containsKey(player);
    }

    public boolean playerInGame(Player player) {
        return playerInGame(player.getUniqueId());
    }

    /**
     * 获取玩家所在的游戏
     *
     * @param player Player
     * @return Possibly null game
     */
    public Game getPlayerGame(UUID player) {
        return games.get(player);
    }

    public Game getPlayerGame(Player player) {
        return getPlayerGame(player.getUniqueId());
    }

    /**
     * 将玩家加入一个队伍
     *
     * @param player Player
     * @param party  Party
     * @return {@code true} if the player was not already in a party
     */
    public boolean playerJoinParty(UUID player, Party party) {
        return parties.putIfAbsent(player, party) == null;
    }

//    /**
//     * 玩家开启队伍频道
//     *
//     * @param player
//     * @return
//     */
//    public boolean enablePartyChannel(UUID player) {
//        return enablesPartyChannel.add(player);
//    }
//
//    /**
//     * 玩家关闭队伍频道
//     *
//     * @param player
//     * @return
//     */
//    public boolean disablePartyChannel(UUID player) {
//        return enablesPartyChannel.remove(player);
//    }

    /**
     * 玩家是否加入了队伍频道
     *
     * @param player
     * @return
     */
    public boolean isPlayerEnabledPartyChannel(UUID player) {
        return enablesPartyChannel.contains(player);
    }

    /**
     * 让玩家离开队伍
     *
     * @param player Player
     * @return {@code true} if the player was in a party
     */
    public boolean playerLeaveParty(UUID player) {
//        disablePartyChannel(player);
        return parties.remove(player) != null;
    }

    /**
     * 强制设置玩家的队伍
     * 不明白请勿随便调用
     *
     * @param player
     * @param party
     */
    public void setPlayerParty(Player player, Party party) {
        parties.put(player.getUniqueId(), party);
    }


    /**
     * 让玩家加入一个游戏
     *
     * @param player Player
     * @param game   Game
     * @return {@code true} if the player was not already in a game
     */
    public boolean joinGame(UUID player, Game game) {
        return games.putIfAbsent(player, game) == null;
    }

    /**
     * 让玩家离开他当前游戏
     *
     * @param player Player
     * @return {@code true} if the player was in a game
     */
    public boolean leaveGame(UUID player) {
        return games.remove(player) != null;
    }

    /**
     * 当前游戏中是否有给定的游戏
     *
     * @param name
     * @return
     */
    public boolean containesGame(String name) {
        for (Game game : games.values()) {
            if (game.getGameName().equals(name)) return true;
        }
        return false;
    }

    /**
     * 获取指定的游戏
     *
     * @param name
     * @return
     */
    public Game getGame(String name) {
        for (Game game : games.values()) {
            if (game.getGameName().equals(name)) return game;
        }
        return null;
    }

    public void removeGame(String name) {
        Object[] keys = games.keySet().toArray().clone();
        for (Object key : keys) {
            if (games.get(key).getGameName().equals(name)) {
                games.remove(key);
            }
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Party party = getPlayerParty(event.getPlayer().getUniqueId());
        Game game = getPlayerGame(event.getPlayer().getUniqueId());

        if (party != null) party.onPlayerJoin(event.getPlayer());

        if (game != null) game.onPlayerJoin(event);
        else {
            // 强制玩家 在大厅中生成
            Boolean forceSpawnLobby = getPlugin().getConfig().getBoolean("settings.force-spawn-lobby", false);
            if (forceSpawnLobby) {
                Location lobby = Utils.loadLocation(getPlugin().getConfig().getConfigurationSection("lobby"));
                event.getPlayer().setBedSpawnLocation(lobby, true);
                event.getPlayer().teleport(lobby);
            }
        }

    }



    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Party party = getPlayerParty(event.getPlayer().getUniqueId());
        Game game = getPlayerGame(event.getPlayer().getUniqueId());

        if (party != null) {
            party.onPlayerQuit(event.getPlayer());

        }

        if (game != null) game.onPlayerQuit(event);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerMove(event);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerDropItem(event);
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerGameModeChange(event);
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerToggleFlight(event);
    }

    @EventHandler
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent event) {
        Game game = getPlayerGame(event.getEntity().getUniqueId());
        if (game != null) game.onPlayerFoodLevelChange(event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerInteract(event);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerInteractEntity(event);
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerArmorStandManipulate(event);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Game game = getPlayerGame(event.getEntity().getUniqueId());
            if (game != null) game.onPlayerShootBow(event);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Game game = getPlayerGame(((Player) event.getEntity().getShooter()).getUniqueId());
            if (game != null) game.onProjectileHit(event);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Game game = getPlayerGame(event.getEntity().getUniqueId());
            if (game != null) game.onPlayerDamage(event);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Game game = getPlayerGame(event.getEntity().getUniqueId());
            Party party = getPlayerParty(event.getEntity().getUniqueId());
            // 优先处理游戏中的伤害处理
            // 其次处理队伍中的伤害处理
            if (game != null) game.onPlayerDamageByEntity(event);
            else if (party != null) party.onPlayerDamageByEntity(event);

        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().hasPermission("artimc.place.barrier") && (event.getBlock().getType() == Material.BARRIER || event.getBlock().getType() == Material.LEGACY_BARRIER))
            event.setCancelled(true);

        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onBlockPlace(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onBlockBreak(event);
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        for (Game game : new HashSet<>(games.values()))
            game.onEntityChangeBlock(event);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerItemConsume(event);
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerBucketEmpty(event);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerRespawn(event);
    }

    @EventHandler
    public void onPlayerDead(PlayerDeathEvent event) {
        Game game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerDeath(event);
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
//        Party party = getPlayerParty(event.getPlayer().getUniqueId());
//        if (!event.getMessage().isBlank() && party != null) party.onPlayerChat(event);
    }

    @EventHandler
    public void onPlayCommandSend(PlayerCommandPreprocessEvent event) {
//        for (String cmd : getPlugin().getConfig().getStringList("chat.leave-command")) {
//            if (event.getMessage().equals(cmd)) {
//                disablePartyChannel(event.getPlayer().getUniqueId());
//            }
//        }
    }

//    @EventHandler
//    public void onExplode(BlockExplodeEvent event) {
//        //event.getBlock().getWorld()
//        for (Game game : games.values()) {
//            if (game.getGameMap().getWorldName().equals(event.getBlock().getWorld().getName())) {
//
//            }
//        }
//    }
}
