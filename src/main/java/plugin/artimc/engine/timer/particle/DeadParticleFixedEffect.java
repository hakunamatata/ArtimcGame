package plugin.artimc.engine.timer.particle;

import org.bukkit.Location;
import plugin.artimc.engine.IGame;

/**
 * 死亡特效
 */
public class DeadParticleFixedEffect extends FixedParticle {

    public DeadParticleFixedEffect(Location location, IGame game) {
        super("soul", "popper", location, 1, game);
    }
}
