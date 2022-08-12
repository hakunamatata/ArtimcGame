package plugin.artimc.engine.timer;

public class WaitTimer extends Timer {

    public WaitTimer(int period) {
        super(Timer.WAIT_TIMER, period);
    }

    @Override
    public String getName() {
        return Timer.WAIT_TIMER;
    }
}
