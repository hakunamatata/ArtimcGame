package plugin.artimc.engine.timer.internal;

import plugin.artimc.engine.CloseReason;
import plugin.artimc.engine.StatusBar;
import plugin.artimc.engine.timer.StatusTimer;
import plugin.artimc.engine.timer.TimerManager;

public class FinishPeriodTimer extends StatusTimer {
    public FinishPeriodTimer(int period, StatusBar statusBar) {
        super(TimerManager.FINISH_PERIOD_TIMER, period, statusBar);
    }

    /**
     * 结算时间到
     */
    @Override
    protected void onFinish() {
        // 关闭游戏，游戏正常关闭
        if (getGame().isFinish()) {
            getGame().setCloseReason(CloseReason.GAME_FINISH);
        }
        super.onFinish();
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
        getGame().onFinishPeriodUpdate(this);
    }
}
