package plugin.artimc.engine.timer;

import java.util.EventObject;

public class TimerEvent extends EventObject {

    @Override
    public Timer getSource() {
        return (Timer) super.getSource();
    }

    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    public TimerEvent(Object source) {
        super(source);
    }
}
