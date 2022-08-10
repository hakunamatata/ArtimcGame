package plugin.artimc.engine.timer;

import java.util.EventListener;

public interface TimerListener extends EventListener {

    void onTimerTick(TimerEvent event);

}
