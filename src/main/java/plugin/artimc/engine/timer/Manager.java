package plugin.artimc.engine.timer;

import plugin.artimc.engine.RunnableEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager
 * 描述：Timer 管理器
 * 作者：Leo
 * 创建时间：2022/08/07 22:34
 */
public class Manager {


    private Map<String, Timer> timers;
    private RunnableEngine runnableEngine;

    public Manager(RunnableEngine runnableEngine) {
        this.runnableEngine = runnableEngine;
        this.timers = new HashMap();
    }

    public Map<String, Timer> getTimers() {
        return timers;
    }

    public void startTimer(Timer timer) {
        timer.addEventListener(runnableEngine);
        timers.put(timer.getName(), timer);
        timer.start();
    }

    public void stopTimer(Timer timer) {
        if (timer != null) {
            timer.finish();
            timers.remove(timer.getName());
        }
    }

    public void stopTimer(String timerName) {
        stopTimer(timers.get(timerName));
    }

    public void tick() {
        for (Timer timer : timers.values()) {
            timer.tick();
        }
    }
}
