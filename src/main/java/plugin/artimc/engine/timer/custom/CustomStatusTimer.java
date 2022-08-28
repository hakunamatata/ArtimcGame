package plugin.artimc.engine.timer.custom;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import plugin.artimc.engine.GameRunnable;
import plugin.artimc.engine.timer.GameTimer;
import plugin.artimc.engine.timer.StatusTimer;
import plugin.artimc.engine.timer.TimerManager;

public class CustomStatusTimer extends StatusTimer {

    private final String title;
    private final BarColor barColor;
    private final BarStyle barStyle;

    public CustomStatusTimer(int period, String title, BarColor barColor, BarStyle barStyle, GameRunnable game) {
        super(CustomTimer.RESOURCE_DROP_TIMER, period, game.getStatusBar());
        this.title = title;
        this.barColor = barColor;
        this.barStyle = barStyle;
    }

    public CustomStatusTimer(int period, String title, GameRunnable game) {
        this(period, title, null, null, game);
    }

    public CustomStatusTimer(int period, GameRunnable game) {
        this(period, null, null, null, game);
    }

    public CustomStatusTimer(int period, String title, BarColor barColor, GameRunnable game) {
        this(period, title, barColor, null, game);
    }

    private void changeStatusBar() {
        if (title != null) {
            getStatusBar().setTitle(title);
        }

        if (barColor != null) {
            getStatusBar().setColor(barColor);
        }

        if (barStyle != null) {
            getStatusBar().setStyle(barStyle);
        }

    }

    @Override
    protected void onStart() {
        getStatusBar().setSyncDefaultStatusBar(false);
        super.onStart();
    }

    @Override
    protected void onUpdate() {
        changeStatusBar();
        getGame().onGameTimerUpdate(this);
        getStatusBar().setProgress((double) getCurrent() / (double) getPeriod());
    }

    @Override
    protected void onFinish() {
        getStatusBar().setSyncDefaultStatusBar(true);
        super.onFinish();
    }
}
