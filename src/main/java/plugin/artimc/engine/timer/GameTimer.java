package plugin.artimc.engine.timer;

import org.bukkit.plugin.Plugin;
import plugin.artimc.ArtimcPlugin;
import plugin.artimc.engine.GameRunnable;
import plugin.artimc.engine.IGame;

import java.util.UUID;

/**
 * 游戏内的各种倒计时功能
 */
public abstract class GameTimer {
    private UUID uuid;
    /**
     * 这是计时器的名称
     */
    private String name;
    private TimerState timerState;
    private int period;
    private int current;
    private IGame game;
    private TimerListener listener;

    public GameTimer(String name, int period, IGame game) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.period = period;
        this.current = period;
        this.timerState = TimerState.PAUSED;
        this.game = game;
    }

    public GameTimer(int period, IGame game) {
        this.uuid = UUID.randomUUID();
        this.name = this.uuid.toString();
        this.period = period;
        this.current = period;
        this.timerState = TimerState.PAUSED;
        this.game = game;
    }

    public GameTimer(int period) {
        this.uuid = UUID.randomUUID();
        this.name = this.uuid.toString();
        this.period = period;
        this.current = period;
        this.timerState = TimerState.PAUSED;
    }

    public IGame getGame() {
        return game;
    }

    public void setGame(IGame game) {
        this.game = game;
    }

    public ArtimcPlugin getPlugin() {
        return game.getPlugin();
    }

    protected TimerManager getTimerManager() {
        return getGame().getTimerManager();
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public boolean isRunning() {
        return timerState.equals(TimerState.RUNNING);
    }

    public boolean isPaused() {
        return timerState.equals(TimerState.PAUSED);
    }

    public TimerState getTimerState() {
        return timerState;
    }

    public String getName() {
        return name;
    }

    public int getCurrent() {
        return current;
    }

    public int getPeriod() {
        return period;
    }

    /**
     * 获取计时器过了多少秒
     * n 秒
     *
     * @return
     */
    public int getPassedTime() {
        return getPeriod() - getCurrent();
    }

    public void addEventListener(TimerListener listener) {
        this.listener = listener;
    }

    public void removeEventListener(TimerListener listener) {
        this.listener = null;
    }

    /**
     * 计时器停止，调用结束事假
     */
    public void stop() {
        finish();
    }

    public void tick() {
        current--;
        if (current < 0) {
            this.finish();
        } else {
            this.update();
        }
    }

    public void pause() {
        timerState = TimerState.PAUSED;
    }

    public void unPause() {
        timerState = TimerState.RUNNING;
    }


    public void start() {
        timerState = TimerState.RUNNING;
        try {
            onStart();
            if (listener != null) listener.onTimerStart(this);
        } catch (Exception ex) {
            getPlugin().getLogger().warning(ex.getMessage());
        }
    }

    private void update() {
        try {
            onUpdate();
            if (listener != null) listener.onTimerUpdate(this);
        } catch (Exception ex) {
            getPlugin().getLogger().warning(ex.getMessage());
        }
    }

    public void finish() {
        try {
            onFinish();
            if (listener != null) {
                listener.onTimerFinish(this);
                removeEventListener(listener);
            }
            game.getTimerManager().closeTimer(getName());
        } catch (Exception ex) {
            getPlugin().getLogger().warning(ex.getMessage());
        }

    }

    protected abstract void onStart();

    protected abstract void onFinish();

    protected abstract void onUpdate();

}
