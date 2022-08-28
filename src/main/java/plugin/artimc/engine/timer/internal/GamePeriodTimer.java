package plugin.artimc.engine.timer.internal;

import plugin.artimc.engine.FinishReason;
import plugin.artimc.engine.StatusBar;
import plugin.artimc.engine.timer.StatusTimer;
import plugin.artimc.engine.timer.TimerManager;

public class GamePeriodTimer extends StatusTimer {

    public GamePeriodTimer(int period, StatusBar statusBar) {
        super(TimerManager.GAME_PERIOD_TIMER, period, statusBar);
    }

    /**
     * 游戏进行结束
     * 如果还在游戏中，游戏结束
     */
    @Override
    protected void onFinish() {
        if (getGame().isGaming()) {
            // 结束游戏：游戏时间到
            getGame().setFinishReason(FinishReason.GAMING_TIMEOUT);
        }
        super.onFinish();
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
        getGame().onGamePeriodUpdate(this);
    }
}
