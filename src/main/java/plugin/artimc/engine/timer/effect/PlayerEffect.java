package plugin.artimc.engine.timer.effect;

import org.bukkit.entity.Player;
import plugin.artimc.engine.GameRunnable;
import plugin.artimc.engine.IGame;
import plugin.artimc.engine.timer.AbstractPlayerEffect;

public class PlayerEffect extends AbstractPlayerEffect {

    public static final String PVP_GAME_RESPAWN_INVINCIBLE = "PVP_GAME_RESPAWN_INVINCIBLE";

    public PlayerEffect(String effectName, Player player, int period, IGame game) {
        super(effectName, player, period, game);
    }

}
