package plugin.artimc.engine.timer;

import java.util.EventListener;

public interface TimerListener extends EventListener {

    void onTimerStart(GameTimer timer);

    void onTimerUpdate(GameTimer timer);

    void onTimerFinish(GameTimer timer);

}
