package plugin.artimc.engine.timer;

import plugin.artimc.engine.GameRunnable;
import plugin.artimc.engine.timer.effect.PlayerEffect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 自定义计时器管理器
 */
public class TimerManager {
    public static final String WAIT_PERIOD_TIMER = "WAITING_PERIOD_TIMER";
    public static final String GAME_PERIOD_TIMER = "GAMING_PERIOD_TIMER";
    public static final String FINISH_PERIOD_TIMER = "FINISH_PERIOD_TIMER";
    private final GameRunnable gameRunnable;

    public Set<GameTimer> getTimers() {
        return Set.of(timers.values().toArray(new GameTimer[0]));
    }

    private final Map<String, GameTimer> timers;

    public TimerManager(GameRunnable gameRunnable) {
        this.gameRunnable = gameRunnable;
        this.timers = new HashMap<>();
    }

    protected void log(String message) {
        gameRunnable.log(message);
    }

    public GameRunnable getGameRunnable() {
        return gameRunnable;
    }

    /**
     * 添加一个计时器
     * 计时器并不会立即运行
     *
     * @param timer
     */
    public void addTimer(GameTimer timer) {
        if (timers.containsKey(timer.getName())) {
            timers.get(timer.getName()).setCurrent(timer.getCurrent());
            timers.get(timer.getName()).setPeriod(timer.getPeriod());
        } else {
            timers.put(timer.getName(), timer);
        }
    }

    /**
     * 获取计时器实例
     *
     * @param timerName
     * @return
     */
    public GameTimer get(String timerName) {
        if (timers.containsKey(timerName)) return timers.get(timerName);
        return null;
    }

    /**
     * 是否包含计时器
     *
     * @param timerName
     * @return
     */
    public boolean contains(String timerName) {
        return timers.containsKey(timerName);
    }

    /**
     * 计时器是否在运行
     *
     * @param timerName
     * @return
     */
    public boolean isTimerRunning(String timerName) {
        if (contains(timerName)) {
            return timers.get(timerName).isRunning();
        }
        return false;
    }

    /**
     * 启动一个计时器
     *
     * @param timer
     */
    public void startTimer(GameTimer timer) {
        addTimer(timer);
        timer.start();
        if (timer instanceof PlayerEffect) {
            PlayerEffect e = (PlayerEffect) timer;
            log(String.format("计时器 启动: %s %s", e.getPlayer().getName(), e.getEffectName()));
        } else log(String.format("计时器 启动: %s", timer.getName()));
    }

    /**
     * 启动一个计时器
     *
     * @param timerName
     */
    public void startTimer(String timerName) {
        if (timers.containsKey(timerName)) {
            timers.get(timerName).start();
            log(String.format("计时器 启动: %s", timerName));
        }
    }

    /**
     * 强制停止一个计时器
     * 触发计时器停止事件
     *
     * @param timer
     */
    public void stopTimer(GameTimer timer) {
        timer.stop();
    }

    /**
     * 强制停止一个计时器
     * 触发计时器停止事件
     *
     * @param timerName
     */
    public void stopTimer(String timerName) {
        if (timers.containsKey(timerName)) {
            stopTimer(timers.get(timerName));
        }
    }

    /**
     * 暂停一个计时器
     *
     * @param timer
     */
    public void pauseTimer(GameTimer timer) {
        timer.pause();
    }

    /**
     * 暂停一个计时器
     *
     * @param timerName
     */
    public void pauseTimer(String timerName) {
        if (timers.containsKey(timerName)) {
            pauseTimer(timers.get(timerName));
        }
    }

    /**
     * 结束暂停一个计时器
     *
     * @param timer
     */
    public void unPauseTimer(GameTimer timer) {
        timer.unPause();
    }

    /**
     * 结束暂停一个计时器
     *
     * @param timerName
     */
    public void unPauseTimer(String timerName) {
        if (timers.containsKey(timerName)) {
            unPauseTimer(timers.get(timerName));
        }
    }

    /**
     * 强制关闭计时器
     * 计时器将被直接删除
     *
     * @param timerName
     */
    public void closeTimer(String timerName) {
        if (timers.containsKey(timerName)) {
            timers.remove(timerName);
            log(String.format("计时器 关闭: %s", timerName));
        }
    }

    /**
     * 等待玩家加入
     */
    public void waitingCompanions() {
        if (contains(WAIT_PERIOD_TIMER)) {
            startTimer(WAIT_PERIOD_TIMER);
        }
    }

    /**
     * 开始游戏
     */
    public void startingGame() {
        if (contains(WAIT_PERIOD_TIMER)) {
            closeTimer(WAIT_PERIOD_TIMER);
        }
        if (contains(GAME_PERIOD_TIMER)) {
            startTimer(GAME_PERIOD_TIMER);
        }
    }

    /**
     * 游戏结束
     */
    public void finishingGame() {
        if (contains(GAME_PERIOD_TIMER)) {
            closeTimer(GAME_PERIOD_TIMER);
        }
        if (contains(FINISH_PERIOD_TIMER)) {
            startTimer(FINISH_PERIOD_TIMER);
        }
    }

    public void clear() {
        timers.clear();
    }
}
