package plugin.artimc.engine;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import plugin.artimc.ArtimcManager;
import plugin.artimc.ArtimcPlugin;
import plugin.artimc.common.IComponent;
import plugin.artimc.engine.event.PartyJoinGameEvent;
import plugin.artimc.engine.event.PartyLeaveGameEvent;
import plugin.artimc.engine.event.PlayerJoinGameEvent;
import plugin.artimc.engine.event.PlayerLeaveGameEvent;
import plugin.artimc.engine.timer.GameTimer;
import plugin.artimc.engine.timer.TimerManager;
import plugin.artimc.engine.timer.custom.CustomTimer;
import plugin.artimc.engine.timer.effect.PlayerEffect;
import plugin.artimc.engine.timer.particle.FixedParticle;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public interface IGame extends Runnable, IComponent, AutoCloseable {

    FileConfiguration getConfig();

    String getName();

    GameMap getMap();

    String getWorldName();

    Set<UUID> getPlayers();

    Server getServer();

    ArtimcPlugin getPlugin();

    ArtimcManager getManager();

    Logger getLogger();

    Set<Player> getOnlinePlayers();

    int getGameLeftTime();

    int getGamePassedTime();

    TimerManager getTimerManager();

    void setNameTagManager(NameTagManager nameTagManager);

    void useMechanisms(Mechanism... mechanisms);

    Set<Mechanism> getMechanisms();

    boolean isWaiting();

    boolean isGaming();

    boolean isFinish();

    boolean isClosing();

    int getCurrentTick();

    void addCompanion(Player player);

    void removeCompanion(Player player);

    void sendMessage(String message);

    void onPlayerJoinGame(PlayerJoinGameEvent event);

    void onPlayerLeaveGame(PlayerLeaveGameEvent event);

    void onPartyJoinGame(PartyJoinGameEvent event);

    void onPartyLeaveGame(PartyLeaveGameEvent event);

    void givePlayerEffect(String name, int period, Player player);

    void givePlayerEffect(PlayerEffect effect);

    void createFixedParticle(FixedParticle particleTimer);

    void startTimer(CustomTimer timer);

    void stopTimer(CustomTimer timer);

    boolean playerHasEffect(String name, Player player);

    void removePlayerEffect(String name, Player player);

    @NotNull Location getRespawnLocation(Player player);

    /**
     * This will be called when a player in this game joins the server
     *
     * @param event PlayerJoinEvent
     */
    void onPlayerJoin(final PlayerJoinEvent event);

    /**
     * This will be called when a player in this game quits the server
     *
     * @param event PlayerQuitEvent
     */
    void onPlayerQuit(final PlayerQuitEvent event);

    /**
     * This will be called when a player in this game moves
     *
     * @param event PlayerMoveEvent
     */
    void onPlayerMove(final PlayerMoveEvent event);

    /**
     * This will be called when a player in this game drops an item
     *
     * @param event PlayerDropItemEvent
     */
    void onPlayerDropItem(final PlayerDropItemEvent event);

    /**
     * This will be called when a player in this game changes game mode
     *
     * @param event PlayerGameModeChangeEvent
     */
    void onPlayerGameModeChange(final PlayerGameModeChangeEvent event);

    /**
     * This will be called when a player in this game toggles flight
     *
     * @param event PlayerToggleFlightEvent
     */
    void onPlayerToggleFlight(final PlayerToggleFlightEvent event);

    /**
     * This will be called when a player in this game's hunger changes
     *
     * @param event FoodLevelChangeEvent
     */
    void onPlayerFoodLevelChange(final FoodLevelChangeEvent event);

    /**
     * This will be called when a player in this game interacts with something
     *
     * @param event PlayerInteractEvent
     */
    void onPlayerInteract(final PlayerInteractEvent event);

    /**
     * This will be called when a player in this game interacts with an entity
     *
     * @param event PlayerInteractEntityEvent
     */
    void onPlayerInteractEntity(final PlayerInteractEntityEvent event);

    /**
     * This will be called when a player in this game manipulates an armor stand
     *
     * @param event PlayerArmorStandManipulateEvent
     */
    void onPlayerArmorStandManipulate(final PlayerArmorStandManipulateEvent event);

    /**
     * This will be called when a player in this game shoots a bow
     *
     * @param event EntityShootBowEvent
     */
    void onPlayerShootBow(final EntityShootBowEvent event);

    void onEntityShootBow(EntityShootBowEvent event);

    /**
     * This will be called when a projectile shot by a player in this game hits
     * something
     *
     * @param event ProjectileHitEvent
     */
    void onProjectileHit(final ProjectileHitEvent event);

    /**
     * This will be called when a player in this game takes damage
     *
     * @param event EntityDamageEvent
     */
    void onPlayerDamage(final EntityDamageEvent event);

    /**
     * This will be called when a player in this game is damaged by an entity
     *
     * @param event EntityDamageByEntityEvent
     */
    void onPlayerDamageByEntity(final EntityDamageByEntityEvent event);

    void onEntityDamage(EntityDamageEvent event);

    void onEntityDamageByEntity(EntityDamageByEntityEvent event);

    /**
     * This will be called when a player places a block
     *
     * @param event BlockPlaceEvent
     */
    void onBlockPlace(final BlockPlaceEvent event);

    /**
     * This will be called when a player breaks a block
     *
     * @param event BlockBreakEvent
     */
    void onBlockBreak(final BlockBreakEvent event);


    void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event);

    void onPlayerItemConsume(final PlayerItemConsumeEvent event);

    /**
     * This will be called when an entity changes a block
     *
     * @param event EntityChangeBlockEvent
     */
    void onEntityChangeBlock(final EntityChangeBlockEvent event);

    /**
     * This will be called when an player dead
     *
     * @param event Entity Death Event
     */
    void onPlayerDeath(final PlayerDeathEvent event);

    /**
     * 玩家复活时候执行
     *
     * @param event
     */
    void onPlayerRespawn(final PlayerRespawnEvent event);

    void onPlayerChat(PlayerChatEvent event);

    void onPlayCommandSend(PlayerCommandPreprocessEvent event);

    void onGameTimerUpdate(GameTimer gameTimer);

    void setCloseReason(CloseReason closeReason);

    void onFinishPeriodUpdate(GameTimer gameTimer);

    void setFinishReason(FinishReason finishReason);

    void onGamePeriodUpdate(GameTimer gameTimer);

    void onWaitPeriodUpdate(GameTimer gameTimer);
}
