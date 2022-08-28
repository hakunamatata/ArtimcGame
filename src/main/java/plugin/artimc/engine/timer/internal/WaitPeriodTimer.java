package plugin.artimc.engine.timer.internal;

import plugin.artimc.engine.CloseReason;
import plugin.artimc.engine.StatusBar;
import plugin.artimc.engine.timer.StatusTimer;
import plugin.artimc.engine.timer.TimerManager;

public class WaitPeriodTimer extends StatusTimer {
    public WaitPeriodTimer(int period, StatusBar statusBar) {
        super(TimerManager.WAIT_PERIOD_TIMER, period, statusBar);
    }

    /**
     * 等待时间到
     * 如果还没开始，设置游戏关闭
     */
    @Override
    protected void onFinish() {
        if (getGame().isWaiting()) {
            getGame().setCloseReason(CloseReason.NO_COMPANION);
        }
        super.onFinish();
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
        getGame().onWaitPeriodUpdate(this);
        if (getGame().isWaiting()) {
            if (getGame().getOnlinePlayers().isEmpty()) {
                getGame().setCloseReason(CloseReason.NO_ONLINE_PLAYERS);
            }
        }
    }
}
