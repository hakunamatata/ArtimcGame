package plugin.artimc.engine;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * BuffEffect 针对玩家的一种计时器
 */
public abstract class GameBuffEffect extends GameTimer {
    Player player;

    String buffName;

    public GameBuffEffect(String buffName, Player player, int period, Game game) {
        super(period, game);
        this.buffName = buffName;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public String getBuffName() {
        return buffName;
    }

    /**
     * 获取当前玩家的所有Buff效果
     *
     * @return
     */
    private Set<GameBuffEffect> getPlayerEffects() {
        Set<GameBuffEffect> effects = new HashSet<>();
        for (GameTimer timer : getGame().getTimerManager().values()) {
            if (timer instanceof GameBuffEffect) {
                GameBuffEffect effect = (GameBuffEffect) timer;
                if (effect.getPlayer().equals(getPlayer())) {
                    effects.add(effect);
                }
            }
        }
        return effects;
    }

    @Override
    protected void onStart() {
        // 如果玩家被赋予相同的效果
        // 更新现有效果的持续时间
        // 而不是添加
        boolean exist = false;
        for (GameTimer timer : getGame().getTimerManager().values()) {
            if (timer instanceof GameBuffEffect) {
                GameBuffEffect effect = (GameBuffEffect) timer;
                if (effect.getPlayer().equals(getPlayer()) && effect.buffName.equals(getBuffName())) {
                    effect.setCurrent(getPeriod());
                    effect.setPeriod(getPeriod());
                    exist = true;
                    break;
                }
            }
        }
        if (!exist) super.onStart();
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
    }

    protected void onFinish() {
        // removePlayerBuff();
        super.onFinish();
    }
}
