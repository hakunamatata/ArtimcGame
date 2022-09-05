package plugin.artimc.engine.timer.custom;

import plugin.artimc.engine.GameRunnable;
import plugin.artimc.engine.IGame;
import plugin.artimc.engine.timer.GameTimer;

public class CustomTimer extends GameTimer {
    public static final String RESOURCE_DROP_TIMER = "RESOURCE_DROP_TIMER";

    public CustomTimer(String name, int period, GameRunnable game) {
        super(name, period, game);
    }

    public CustomTimer(int period, IGame game) {
        super(period, game);
    }

    public CustomTimer(int period) {
        super(period);
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onFinish() {
        getGame().onGameTimerFinish(this);
    }

    @Override
    protected void onUpdate() {
        getGame().onGameTimerUpdate(this);
    }
}
