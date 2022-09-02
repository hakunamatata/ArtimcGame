package plugin.artimc.engine.timer;

import dev.esophose.playerparticles.particles.FixedParticleEffect;
import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.styles.ParticleStyle;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import plugin.artimc.engine.IGame;

public abstract class AbstractParticle extends GameTimer {

    private final ConsoleCommandSender consoleSender;
    private final ParticleEffect effect;
    private final ParticleStyle style;
    private final Location location;
    private FixedParticleEffect instance;

     public AbstractParticle(ParticleEffect effect, ParticleStyle style, Location location, int period, IGame game) {
        super(period, game);
        this.consoleSender = getPlugin().getServer().getConsoleSender();
        this.location = location;
        this.effect = effect;
        this.style = style;
    }

    @Override
    protected void onStart() {
        instance = getPlugin().getParticlesAPI().createFixedParticleEffect(consoleSender, location, effect, style);
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onFinish() {
        getPlugin().getParticlesAPI().removeFixedEffect(consoleSender, instance.getId());
    }
}
