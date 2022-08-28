package plugin.artimc.engine.timer.custom;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import plugin.artimc.engine.GameRunnable;
import plugin.artimc.engine.timer.TimerManager;
import plugin.artimc.instance.LogFactoryGame;

public class ResourceDropTimer extends CustomStatusTimer {

    public ResourceDropTimer(int period, String title, BarColor barColor, BarStyle barStyle, GameRunnable game) {
        super(period, title, barColor, barStyle, game);
    }

    public ResourceDropTimer(int period, String title, GameRunnable game) {
        super(period, title, game);
    }

    public ResourceDropTimer(int period, GameRunnable game) {
        super(period, game);
    }

    public ResourceDropTimer(int period, String title, BarColor barColor, GameRunnable game) {
        super(period, title, barColor, game);
    }

    @Override
    protected void onStart() {
        //getGame().getTimerManager().pauseTimer(TimerManager.GAME_PERIOD_TIMER);
        super.onStart();
    }

    @Override
    protected void onFinish() {
        if (getGame() instanceof LogFactoryGame) {
            LogFactoryGame game = (LogFactoryGame) getGame();
            if (game.isGaming()) {
                game.dropItems();
            }
        }
        super.onFinish();
        //getGame().getTimerManager().unPauseTimer(TimerManager.GAME_PERIOD_TIMER);
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
    }
}
