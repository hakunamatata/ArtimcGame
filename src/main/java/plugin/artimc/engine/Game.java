package plugin.artimc.engine;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import plugin.artimc.ArtimcPlugin;
import plugin.artimc.PlayerGameManager;
import plugin.artimc.PlayerPartyManager;
import plugin.artimc.WorldManager;
import plugin.artimc.engine.event.*;
import plugin.artimc.engine.item.GameItem;
import plugin.artimc.engine.listener.IGameListener;
import plugin.artimc.engine.mechanism.PlayerRespawn;
import plugin.artimc.engine.timer.GameTimer;
import plugin.artimc.engine.world.GameWorld;
import plugin.artimc.engine.world.WorldStatus;
import plugin.artimc.scoreboard.BaseScoreboard;
import plugin.artimc.scoreboard.GameScoreboard;
import plugin.artimc.scoreboard.PartyScoreboard;

/**
 * 描述：Game，游戏
 * 处理游戏中的玩家、计分板、游戏事件处理
 * 处理游戏的底层运行，时间刻等
 * 作者：Leo
 * 创建时间：2022/7/29 20:40
 */
public abstract class Game extends GameRunnable implements IGameListener {
    private final Set<Party> winners;
    private final Set<Party> losers;
    private boolean friendlyFire = false;
    private Set<GameItem> gameItems;
    private Companion companion;

    protected Game(String gameName, ArtimcPlugin plugin) {
        super(gameName, plugin);
        this.companion = new Companion(this);
        this.winners = new HashSet<>();
        this.losers = new HashSet<>();
        this.gameItems = new HashSet<>();
        useMechanisms(
                // 玩家复活 5秒内 无法移动
                new PlayerRespawn(this));
    }

    /**
     * 获取游戏地图
     *
     * @return
     */
    public GameMap getGameMap() {
        return gameMap;
    }

    protected void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public String getWorldName() {
        return getGameMap().getWorldName();
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    @Override
    public int getWaitingPeriod() {
        return getGameMap().getWaitPeriod();
    }

    @Override
    public int getGamingPeriod() {
        return getGameMap().getGamePeriod();
    }

    @Override
    public int getFinishPeriod() {
        return getGameMap().getFinishPeriod();
    }

    public Set<Party> getWinners() {
        return winners;
    }

    public Set<Party> getLosers() {
        return losers;
    }


    public Party getObserveParty() {
        return companion.getObserveParty();
    }

    public Map<PartyName, Party> getGameParties() {
        return companion.getGameParties();
    }

    public Set<Party> getParties() {
        return companion.getParties();
    }


    public void showTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : getOnlinePlayers()) {
            player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
        }
    }

    public Companion getCompanion() {
        return companion;
    }

    public void setPlayerInvincible(Player player, boolean invincible) {
        companion.setInvincible(player, invincible);
    }

    public boolean isPlayerInvincible(Player player) {
        return companion.isInvincible(player);
    }


    /**
     * 玩家加入游戏
     * 自动加入队伍，自动加入观察者，整队自动加入游戏
     */
    public void addCompanion(Player player) {
        companion.addCompanion(player);
    }

    /**
     * 将玩家设置为Observer
     *
     * @param player
     */
    public void setPlayerAsObserver(Player player) {
        getObserveParty().sendMessage(getGameLocaleString("ob-join-player").replace("%player_name%", player.getName()));
        player.setGameMode(GameMode.SPECTATOR);
    }

    public void onPartyJoinGame(PartyJoinGameEvent event) {
        // 有新的队伍加入游戏，更新玩家的计分板
        changeGameScoreboard(useScoreboard(), this.getClass());
    }

    protected Class<? extends GameScoreboard> useScoreboard() {
        return GameScoreboard.class;
    }

    // 修改计分板
    private void changeGameScoreboard(Class<? extends GameScoreboard> type, Class<? extends Game> game) {
        // 将游戏内队伍的计分板设置为游戏计分板
        try {
            Constructor<? extends GameScoreboard> ct = type.getDeclaredConstructor(BaseScoreboard.class, game);
            GameScoreboard newScoreboard;
            for (Party party : getParties()) {
                newScoreboard = ct.newInstance(party.getScoreboard(), this);
                party.setScoreboard(newScoreboard);
            }
            // 观察者队伍，使用游戏计分板
            newScoreboard = ct.newInstance(getObserveParty().getScoreboard(), this);
            getObserveParty().setScoreboard(newScoreboard);
        } catch (Exception ex) {
            sendMessage(getGameLocaleString("scoreboard-unsupport: ") + ex.getMessage());
        }
    }


    /**
     * 当游戏从 等待中 切换成 游戏开始 之后执行
     */
    protected void onGameStart() {
        // 修改队伍的计分板
        changeGameScoreboard(useScoreboard(), this.getClass());
        // 修改所有玩家的命名牌显示
        nameTagManager.apply();
    }

    @Override
    protected void onGameFinish(FinishReason finishReason) {
        for (Party party : getParties()) {
            if (willWinTheGame(party)) {
                winners.add(party);
            } else {
                losers.add(party);
            }
        }
        super.onGameFinish(finishReason);
    }

    /**
     * 胜利条件
     *
     * @return
     */
    protected abstract boolean willWinTheGame(Party party);


    /**
     * 获取当前游戏中的在线玩家
     *
     * @return
     */
    public Set<Player> getOnlinePlayers() {
        return getManager().getOnlinePlayers(players);
    }

    public Set<Player> getOnlinePlayersExceptObserver() {
        Set<Player> players = new HashSet<>();
        for (Party party : getParties()) {
            for (Player player : party.getOnlinePlayers()) {
                players.add(player);
            }
        }
        return players;
    }

    /**
     * 设置游戏中玩家是否可以看见其他玩家
     *
     * @param value
     */
    public void setInvisibility(final boolean value) {
        final Set<Player> onlinePlayers = getOnlinePlayers();
        for (final Player player : onlinePlayers) {
            for (final Player p : onlinePlayers) {
                if (p != player) {
                    if (value) player.hidePlayer(getPlugin(), p);
                    else player.showPlayer(getPlugin(), p);
                }
            }
        }
    }

    public GameItem dropItem(ItemStack item, Location location) {
        GameItem gameItem = new GameItem(item, this);
        gameItem.drop(location);
        gameItems.add(gameItem);
        return gameItem;
    }

    /**
     * 玩家离开游戏
     *
     * @param player
     */
    public void removeCompanion(Player player) {
        companion.removeCompanion(player);
        log(String.format("玩家 %s 离开了游戏", player.getName()));
    }

    @Override
    public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
        super.onPlayerLeaveGame(event);

        if (event.getPlayer().isOnline()) {
            Player player = (Player) event.getPlayer();
            player.teleport(getGameMap().getLobby());
        }
    }

    /**
     * 当玩家加入游戏时
     *
     * @param player
     * @param party
     */
    @Override
    public void onPlayerJoinGame(PlayerJoinGameEvent event) {
        super.onPlayerJoinGame(event);

        event.getPlayer().teleport(getGameMap().getSpawn().get("default"));
        if (event.isObserver()) {
            setPlayerAsObserver(event.getPlayer());
        }
    }

    public void onPartyLeaveGame(PartyLeaveGameEvent event) {
        event.getParty().sendMessage(getGameLocaleString("game-is-closed"));
        getCompanion().getGameParties().remove(event.getParty().getPartyName());
        event.getParty().setGame(null);
        event.getParty().setPartyName(null);
        event.getParty().setScoreboard(new PartyScoreboard(getObserveParty().getScoreboard(), event.getParty()));
        event.getParty().updateScoreboard();
    }

    /**
     * 游戏被关闭之前执行
     */
    @Override
    protected void onGameClose(CloseReason closeReason) {
        // 重置地形，如果需要的话
        getGameWorld().reset(getGameMap().getSchemaFile(), getGameMap().getBaseHeight());
        setInvisibility(false);
        // 清理
        clearObserver();
        companion.clear();
        players.clear();
        winners.clear();
        losers.clear();
        playerPlacedBlocks.clear();
        super.onGameClose(closeReason);
    }

    private void clearObserver() {
        Object[] players = getObserveParty().getPlayers().toArray().clone();
        for (Object obj : players) {
            UUID player = (UUID) obj;
            getObserveParty().getScoreboard().remove(player);
            OfflinePlayer offline = getPlugin().getServer().getOfflinePlayer(player);
            resetPlayer((Player) offline);
            getObserveParty().leave(offline);
            removeCompanion(((Player) offline));
        }
    }

    @Override
    public void close() throws Exception {
        log(String.format("游戏已释放"));
        super.close();
    }

    /**
     * 固定每秒运行
     */
    @Override
    protected void onFixedUpdate() {
        for (final Party party : getParties()) {
            party.updateScoreboard();
        }
        // 实时更新观察者队伍计分板
        updateObservers();
    }

    @Override
    public void onWaitPeriodUpdate(GameTimer timer) {
        super.onWaitPeriodUpdate(timer);
    }

    @Override
    public void onGamePeriodUpdate(GameTimer timer) {
        super.onGamePeriodUpdate(timer);
    }

    @Override
    public void onGameTimerUpdate(GameTimer timer) {
        super.onGameTimerUpdate(timer);
    }

    @Override
    public void onFinishPeriodUpdate(GameTimer timer) {
        super.onFinishPeriodUpdate(timer);
    }

    private void updateObservers() {
        getObserveParty().updateScoreboard();
        for (Player player : getObserveParty().getOnlinePlayers()) {
            if (player.getGameMode() != GameMode.SPECTATOR) {
                player.setGameMode(GameMode.SPECTATOR);
            }
        }
    }

    protected void resetPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        // 移除所有药水效果
        for (PotionEffect pe : player.getActivePotionEffects())
            player.removePotionEffect(pe.getType());
        player.setGlowing(false);
        player.setHealthScaled(false);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        setPlayerInvincible(player, false);
    }

    /**
     * 需要确保时有玩家在场
     * 当游戏内没有玩家的时候，这个游戏就已经没有存在的必要了
     *
     * @return
     */
    @Override
    protected CloseReason willGameClose() {
        // 除了等待期间以外，如果游戏中没有玩家在线，即可关闭游戏
        if (!isWaiting() && companion.getOnlinePlayers().isEmpty()) {
            return CloseReason.NO_ONLINE_PLAYERS;
        }
        return super.willGameClose();
    }


    /**
     * 玩家是否在出生点保护的范围里
     *
     * @param player
     * @return 返回 所在出生点的所属的队伍
     */
    public Party inSpawnProtection(Player player) {
        return null;
    }

    public boolean contains(Player player) {
        return contains(player.getUniqueId());
    }

    /**
     * This will be called when a player in this game quits the server
     *
     * @param event PlayerQuitEvent
     */
    public void onPlayerQuit(final PlayerQuitEvent event) {
        // 玩家在游戏过程中退出，设置生成位置为大厅
        //event.getPlayer().setBedSpawnLocation(getGameMap().getLobby(), true);
        // 玩家在游戏过程中退出视为离开游戏，且离开队伍
        event.getPlayer().setGlowing(false);
        // 玩家下线 表示离开游戏
        resetPlayer(event.getPlayer());
        removeCompanion(event.getPlayer());
        super.onPlayerQuit(event);
    }

    /**
     * This will be called when a player in this game takes damage
     *
     * @param event EntityDamageEvent
     */
    public void onPlayerDamage(final EntityDamageEvent event) {
        // 游戏未开始时，玩家不受任何伤害
        if (!isGaming()) event.setCancelled(true);
            // 游戏开始时
        else {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                // 如果玩家处于无敌状态，不受伤害
                if (isPlayerInvincible(player)) {
                    event.setCancelled(true);
                }
            }
        }
        super.onPlayerDamage(event);
    }

    /**
     * This will be called when a player in this game takes damage by another player
     */
    public void onPlayerDamageByPlayer(final Player player, final Player damager, final EntityDamageByEntityEvent eventSource) {
    }

    /**
     * This will be called when a player in this game is damaged by an entity
     *
     * @param event EntityDamageByEntityEvent
     */
    public void onPlayerDamageByEntity(final EntityDamageByEntityEvent event) {
        Player player = (Player) event.getEntity();
        Player damager = null;
        // 如果攻击实体为 Player
        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        }
        // 如果攻击实体不是 Player
        else {
            switch (event.getCause()) {
                case MAGIC:
                case PROJECTILE:
                    Projectile proj = (Projectile) event.getDamager();
                    if (proj.getShooter() instanceof Player) {
                        damager = (Player) proj.getShooter();
                    }
                    break;
            }
        }

        if (damager != null) {
            // 如果玩家不承受伤害者的伤害，将伤害取消
            if (!willPlayerTakesDamage(player, damager)) {
                event.setCancelled(true);
            }

            // 即将造成伤害
            if (!event.isCancelled()) {
                onPlayerDamageByPlayer(player, damager, event);
            }
        }
        super.onPlayerDamageByEntity(event);
    }

    protected boolean willPlayerTakesDamage(Player player, Player damager) {
        // 玩家不是无敌的，允许对他造成伤害
        return !isPlayerInvincible(player);
    }

    private final Set<Block> playerPlacedBlocks = new HashSet<>();

    /**
     * This will be called when a player places a block
     *
     * @param event BlockPlaceEvent
     */
    public void onBlockPlace(final BlockPlaceEvent event) {
        // 游戏未开始时，玩家无法放置任何方块
        if (!isGaming()) {
            event.setCancelled(true);
        }
        // 游戏开始之后，只有玩家防止的方块可以摧毁
        else {
            playerPlacedBlocks.add(event.getBlock());
        }

        super.onBlockPlace(event);
    }

    /**
     * This will be called when a player breaks a block
     *
     * @param event BlockBreakEvent
     */
    public void onBlockBreak(final BlockBreakEvent event) {
        // 游戏未开始时，玩家无法破坏任何方块
        if (!isGaming()) {
            event.setCancelled(true);
        }
        // 游戏开始之后，只有玩家防止的方块可以被摧毁
        else {
            if (playerPlacedBlocks.contains(event.getBlock())) {
                playerPlacedBlocks.remove(event.getBlock());
            } else {
                event.setCancelled(true);
            }
        }

        super.onBlockBreak(event);
    }


    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
        if (!isGaming()) {
            event.setCancelled(true);
        }

        super.onPlayerBucketEmpty(event);
    }

    /**
     * This will be called when an entity changes a block
     *
     * @param event EntityChangeBlockEvent
     */
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        // 游戏未开始时，玩家无法破坏任何方块
        if (!isGaming()) event.setCancelled(true);

        super.onEntityChangeBlock(event);

    }


    @Override
    public void onGameItemPickup(GameItemPickupEvent event) {
        if (!event.isCancel()) {
            event.getItem().pickUp(event.getPlayer());
            gameItems.remove(event.getItem());
        }
    }

}
