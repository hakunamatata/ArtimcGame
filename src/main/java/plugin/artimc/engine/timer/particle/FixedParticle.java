package plugin.artimc.engine.timer.particle;

import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.styles.ParticleStyle;
import org.bukkit.Location;
import plugin.artimc.engine.IGame;
import plugin.artimc.engine.timer.AbstractParticle;

public class FixedParticle extends AbstractParticle {

    public FixedParticle(String effectName, String styleName, Location location, int period, IGame game) {
        super(ParticleEffect.fromName(effectName), ParticleStyle.fromName(styleName), location, period, game);
    }

}
