package plugin.artimc.engine.timer;

import org.bukkit.entity.Player;
import plugin.artimc.engine.GameRunnable;
import plugin.artimc.engine.IGame;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractPlayerEffect extends GameTimer {
    private final Player player;
    private final String effectName;

    public AbstractPlayerEffect(String effectName, Player player, int period, IGame game) {
        super(period, game);
        this.effectName = effectName;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public String getEffectName() {
        return effectName;
    }

    private Set<AbstractPlayerEffect> getPlayerEffects() {
        Set<AbstractPlayerEffect> effects = new HashSet<>();
        for (GameTimer timer : getGame().getTimerManager().getTimers()) {
            if (timer instanceof AbstractPlayerEffect) {
                AbstractPlayerEffect effect = (AbstractPlayerEffect) timer;
                if (effect.getPlayer().equals(getPlayer())) {
                    effects.add(effect);
                }
            }
        }
        return effects;
    }

    private Optional<AbstractPlayerEffect> getPlayerEffect(String effectName) {
        return getPlayerEffects().stream().filter(p -> p.getEffectName().equals(effectName)).findAny();
    }

    /**
     * 如果玩家已经有这种效果
     * 更新持续时间
     */
    @Override
    protected void onStart() {
        Optional<AbstractPlayerEffect> effect = getPlayerEffect(getEffectName());
        if (!effect.isEmpty()) {
            effect.get().setCurrent(getCurrent());
            effect.get().setPeriod(getPeriod());
        }
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onFinish() {

    }
}
