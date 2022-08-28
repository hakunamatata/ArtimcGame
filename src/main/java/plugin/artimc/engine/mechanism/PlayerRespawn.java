package plugin.artimc.engine.mechanism;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import plugin.artimc.engine.IGame;
import plugin.artimc.engine.Mechanism;
import plugin.artimc.engine.timer.effect.PlayerEffect;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 玩家复活
 * 高光、不可攻击、不受伤害
 * 移动限制
 */
public class PlayerRespawn extends Mechanism {

    /**
     * 被冻结的玩家
     */
    private final Set<UUID> frozenPlayers;

    public PlayerRespawn(IGame game) {
        super(game);
        frozenPlayers = new HashSet<>();
        getLogger().info(" * use player respawn mechanism");
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (getGame().isGaming()) {
            frozenPlayers.add(event.getPlayer().getUniqueId());
            getGame().givePlayerEffect(new PlayerEffect("player-respawn-mechanism", event.getPlayer(), 5, getGame()) {
                @Override
                protected void onStart() {
                    if (getGame().isGaming()) {
                        frozenPlayers.add(event.getPlayer().getUniqueId());
                        event.getPlayer().sendTitle(String.format(ChatColor.GREEN + "%s 秒后解除限制", getCurrent()), String.format(ChatColor.GRAY + "你刚复活，还很虚弱..."), 10, 10, 10);
                    }
                    super.onStart();
                }

                @Override
                protected void onUpdate() {
                    if (getGame().isGaming()) {
                        event.getPlayer().sendTitle(String.format(ChatColor.GREEN + "%s 秒后解除限制", getCurrent()), String.format(ChatColor.GRAY + "你刚复活，还很虚弱..."), 10, 10, 10);
                    }
                    super.onUpdate();
                }

                @Override
                protected void onFinish() {
                    frozenPlayers.remove(event.getPlayer().getUniqueId());
                    super.onFinish();
                }
            });
        }
        super.onPlayerRespawn(event);
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (getGame().isGaming() && frozenPlayers.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
        super.onPlayerMove(event);
    }

    @Override
    protected void remove() {
        frozenPlayers.clear();
        super.remove();
    }
}
