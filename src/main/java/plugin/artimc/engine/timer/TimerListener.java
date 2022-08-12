package plugin.artimc.engine.timer;

import java.util.EventListener;

public interface TimerListener extends EventListener {

    void onTimerStart(TimerEvent event);

    void onTimerTick(TimerEvent event);

    void onTimerFinish(TimerEvent event);


}
