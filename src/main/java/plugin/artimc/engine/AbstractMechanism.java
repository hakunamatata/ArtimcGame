package plugin.artimc.engine;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import plugin.artimc.common.AbstractComponent;
import plugin.artimc.engine.timer.GameTimer;

public abstract class AbstractMechanism extends AbstractComponent {
    private final IGame game;

    public AbstractMechanism(IGame game) {
        super(game.getPlugin());
        this.game = game;
    }

    public IGame getGame() {
        return game;
    }

    public void onWaitPeriodUpdate(GameTimer timer) {

    }

    public void onGamePeriodUpdate(GameTimer timer) {

    }

    public void onFinishPeriodUpdate(GameTimer timer) {

    }

    public void onGameTimerUpdate(GameTimer timer) {

    }

    public void onPlayerJoin(PlayerJoinEvent event) {

    }


    public void onPlayerQuit(PlayerQuitEvent event) {

    }

    public void onPlayerMove(PlayerMoveEvent event) {

    }

    public void onPlayerDropItem(PlayerDropItemEvent event) {

    }

    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {

    }

    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {

    }

    public void onPlayerFoodLevelChange(FoodLevelChangeEvent event) {

    }

    public void onPlayerInteract(PlayerInteractEvent event) {

    }

    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

    }

    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {

    }

    public void onEntityShootBow(EntityShootBowEvent event) {

    }

    public void onProjectileHit(ProjectileHitEvent event) {

    }

    public void onEntityDamage(EntityDamageEvent event) {

    }

    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

    }

    public void onBlockPlace(BlockPlaceEvent event) {

    }

    public void onBlockBreak(BlockBreakEvent event) {

    }

    public void onEntityChangeBlock(EntityChangeBlockEvent event) {

    }

    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {

    }

    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {

    }

    public void onPlayerRespawn(PlayerRespawnEvent event) {

    }

    public void onPlayerChat(PlayerChatEvent event) {

    }

    public void onPlayCommandSend(PlayerCommandPreprocessEvent event) {

    }

    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
    }

    public void onPlayerDamage(EntityDamageEvent event) {
    }

    public void onPlayerDeath(PlayerDeathEvent event) {

    }

    public void onPlayerShootBow(EntityShootBowEvent event) {

    }
}
