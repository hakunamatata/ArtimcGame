package plugin.artimc;

import java.util.*;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import plugin.artimc.common.IComponent;
import plugin.artimc.engine.IGame;
import plugin.artimc.engine.Party;
import plugin.artimc.utils.Utils;

/**
 * 描述：GameManager，游戏管理器
 * 管理游戏中的玩家、队伍、对游戏事件进行分发
 * 游戏创造器，游戏工厂
 * 作者：Leo
 * 创建时间：2022/7/29 20:40
 */
public class ArtimcManager implements IComponent, Listener {
    private final ArtimcPlugin plugin;
    private final PlayerGameManager playerGameManager;
    private final PlayerPartyManager playerPartyManager;
    private final GameManager gameManager;
    private final WorldManager worldManager;
    private final PlayerChannelManager playerChannelManager;

    public ArtimcManager(ArtimcPlugin plugin) {
        this.plugin = plugin;
        this.playerGameManager = new PlayerGameManager(this);
        this.playerPartyManager = new PlayerPartyManager(this);
        this.gameManager = new GameManager(this);
        this.worldManager = new WorldManager(this);
        this.playerChannelManager = new PlayerChannelManager(this);
    }

    @Override
    public ArtimcManager getManager() {
        return this;
    }

    public ArtimcPlugin getPlugin() {
        return plugin;
    }

    @Override
    public GameManager getGameManager() {
        return gameManager;
    }

    @Override
    public PlayerGameManager getPlayerGameManager() {
        return playerGameManager;
    }

    @Override
    public PlayerPartyManager getPlayerPartyManager() {
        return playerPartyManager;
    }

    @Override
    public PlayerChannelManager getPlayerChannelManager() {
        return playerChannelManager;
    }

    @Override
    public WorldManager getWorldManager() {
        return worldManager;
    }

    @Override
    public Server getServer() {
        return plugin.getServer();
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    public Player getOnlinePlayer(UUID player) {
        return getPlugin().getServer().getPlayer(player);
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
     * 获取玩家所在的队伍
     *
     * @param player Player
     * @return Possibly null party
     */
    @Deprecated
    public Party getPlayerParty(UUID player) {
        return playerPartyManager.get(player);
    }

    @Deprecated
    public Party getPlayerParty(Player player) {
        return getPlayerParty(player.getUniqueId());
    }

    /**
     * 检测玩家是否在任意一个游戏中
     *
     * @param player Player
     * @return {@code true} if the player is in a party
     */
    @Deprecated
    public boolean playerInGame(UUID player) {
        return playerGameManager.contains(player);
//        return playerGames.containsKey(player);
    }

    @Deprecated
    public boolean playerInGame(Player player) {
        return playerInGame(player.getUniqueId());
    }

    /**
     * 获取玩家所在的游戏
     *
     * @param player Player
     * @return Possibly null game
     */
    @Deprecated
    public IGame getPlayerGame(UUID player) {
        return playerGameManager.get(player);
        //return playerGames.get(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Party party = getPlayerParty(event.getPlayer().getUniqueId());
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());

        if (party != null) party.onPlayerJoin(event.getPlayer());

        if (game != null) game.onPlayerJoin(event);
        else {
            // 强制玩家 在大厅中生成
            Boolean forceSpawnLobby = getPlugin().getConfig().getBoolean("settings.force-spawn-lobby", false);
            if (forceSpawnLobby) {
                Location lobby = Utils.loadLocation(getPlugin().getConfig().getConfigurationSection("lobby"));
                //event.getPlayer().setBedSpawnLocation(lobby, true);
                event.getPlayer().teleport(lobby);
            }
        }

    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Party party = getPlayerParty(event.getPlayer().getUniqueId());
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());

        if (party != null) {
            party.onPlayerQuit(event.getPlayer());
        }

        if (game != null) game.onPlayerQuit(event);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerMove(event);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerDropItem(event);
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerGameModeChange(event);
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerToggleFlight(event);
    }

    @EventHandler
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent event) {
        IGame game = getPlayerGame(event.getEntity().getUniqueId());
        if (game != null) game.onPlayerFoodLevelChange(event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerInteract(event);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerInteractEntity(event);
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerArmorStandManipulate(event);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            IGame game = getPlayerGame(event.getEntity().getUniqueId());
            if (game != null) game.onPlayerShootBow(event);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            IGame game = getPlayerGame(((Player) event.getEntity().getShooter()).getUniqueId());
            if (game != null) game.onProjectileHit(event);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            IGame game = getPlayerGame(event.getEntity().getUniqueId());
            if (game != null) game.onPlayerDamage(event);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            IGame game = getPlayerGame(event.getEntity().getUniqueId());
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

        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onBlockPlace(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onBlockBreak(event);
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        for (IGame game : playerGameManager.list())
            game.onEntityChangeBlock(event);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerItemConsume(event);
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerBucketEmpty(event);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerRespawn(event);
    }

    @EventHandler
    public void onPlayerDead(PlayerDeathEvent event) {
        IGame game = getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) game.onPlayerDeath(event);
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Party party = getPlayerPartyManager().get(event.getPlayer().getUniqueId());
        if (!event.getMessage().isBlank() && party != null) party.onPlayerChat(event);
    }

    @EventHandler
    public void onPlayCommandSend(PlayerCommandPreprocessEvent event) {
        if (playerChannelManager.getCommandOfChannels().contains(event.getMessage())) {
            playerChannelManager.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent event) {

    }
}
