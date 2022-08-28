package plugin.artimc.engine.timer;

import plugin.artimc.engine.StatusBar;

public class StatusTimer extends GameTimer {

    private final StatusBar statusBar;

    public StatusTimer(String name, int period, StatusBar statusBar) {
        super(name, period, statusBar.getGame());
        this.statusBar = statusBar;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onFinish() {

    }

    /**
     * 更新进度
     */
    @Override
    protected void onUpdate() {
        if (getStatusBar().isSyncDefaultStatusBar()) {
            statusBar.setProgress((double) getCurrent() / (double) getPeriod());
        }
    }
}
