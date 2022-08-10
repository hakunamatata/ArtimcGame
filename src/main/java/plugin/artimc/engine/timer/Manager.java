package plugin.artimc.engine.timer;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager
 * 描述：Timer 管理器
 * 作者：Leo
 * 创建时间：2022/08/07 22:34
 */
public class Manager {

    public Map<String, Timer> getTimers() {
        return timers;
    }

    private Map<String, Timer> timers;

    public Manager() {
        timers = new HashMap();
    }

    public void tick() {
        for (Timer timer : timers.values()) {
            if (timer.getState() != TimerState.FINISH)
                timer.tick();
        }
    }
}
