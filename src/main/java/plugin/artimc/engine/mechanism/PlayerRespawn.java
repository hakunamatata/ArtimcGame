package plugin.artimc.engine.mechanism;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import plugin.artimc.engine.IGame;
import plugin.artimc.engine.Mechanism;
import plugin.artimc.engine.timer.effect.PlayerEffect;
import plugin.artimc.engine.timer.particle.DeadParticleFixedEffect;
import plugin.artimc.engine.title.BlinkTitle;

import java.util.*;

/**
 * 玩家复活
 * 高光、不可攻击、不受伤害
 * 移动限制
 */
public class PlayerRespawn extends Mechanism {
    /**
     * 被冻结的玩家
     */
    private final Map<UUID, Location> playerDeadLocations;
    private final Set<UUID> spectators;

    public PlayerRespawn(IGame game) {
        super(game);
        playerDeadLocations = new HashMap<>();
        spectators = new HashSet<>();
        getLogger().info(" * use player respawn mechanism");
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        UUID player = event.getPlayer().getUniqueId();
        if (playerDeadLocations.containsKey(player)) {
            // 立即从死亡地点复活
            Location deadLocation = playerDeadLocations.get(player);
            if (deadLocation.getY() <= -64) {
                // 防止掉虚空起不来
                event.setRespawnLocation(getGame().getRespawnLocation(event.getPlayer()));
            } else {
                event.setRespawnLocation(playerDeadLocations.get(player));
            }
        }
        super.onPlayerRespawn(event);
        spectators.add(player);

        // 复活之后, 如果游戏还在进行中
        if (getGame().isGaming()) {
            getGame().givePlayerEffect(new PlayerEffect("player-respawn-mechanism", event.getPlayer(), getGame().getConfig().getInt("settings.respawn-delay ", 10), getGame()) {
                @Override
                protected void onStart() {
                    if (spectators.contains(getPlayer().getUniqueId())) {
                        getPlayer().setGameMode(GameMode.SPECTATOR);
                        getPlayer().sendMessage(String.format(ChatColor.RED + "你被击杀， %s 秒后复活", getCurrent()));
                    }
                    super.onStart();
                }

                @Override
                protected void onFinish() {
                    reset(getPlayer());
                    super.onFinish();
                }
            });
        }
        // 如果游戏已经结束
        else {
            event.getPlayer().setGameMode(GameMode.SURVIVAL);
        }
    }

    private void reset(Player player) {
        playerDeadLocations.remove(player.getUniqueId());
        spectators.remove(player.getUniqueId());
        player.teleport(getGame().getRespawnLocation(player));
        player.setGameMode(GameMode.SURVIVAL);
        player.sendTitlePart(TitlePart.SUBTITLE, Component.text(ChatColor.GREEN + "你已复活"));
        player.sendTitlePart(TitlePart.TIMES, new BlinkTitle());
    }

    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        playerDeadLocations.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
        getGame().createFixedParticle(new DeadParticleFixedEffect(event.getPlayer().getLocation(), getGame()));
        super.onPlayerDeath(event);
    }

    @Override
    protected void remove() {
        playerDeadLocations.clear();
        super.remove();
    }
}
