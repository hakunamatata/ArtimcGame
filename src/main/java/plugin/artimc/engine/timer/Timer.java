package plugin.artimc.engine.timer;

import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * Timer
 * 描述：RunnableEngine 的自定义计时器
 * 作者：Leo
 * 创建时间：2022/08/07 22:34
 */
public abstract class Timer implements AutoCloseable {
    private UUID uuid;
    private String name;
    private int period;
    private int current;
    private TimerListener listener;

    public static String WAIT_TIMER = "GAME_ENGINE_IN_WAIT_PERIOD";
    public static String GAME_TIMER = "GAME_ENGINE_IN_GAME_PERIOD";
    public static String FINISH_TIMER = "GAME_ENGINE_IN_FINISH_PERIOD";

    public Timer(String name, int period) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.period = period;
        this.current = period;
    }

    public String getName() {
        return name;
    }

    public int getPeriod() {
        return period;
    }

    public int getCurrent() {
        return current;
    }

    public Timer(int period) {
        this.uuid = UUID.randomUUID();
        this.name = this.uuid.toString();
        this.period = period;
        this.current = period;
    }

    public void addEventListener(TimerListener listener) {
        this.listener = listener;
    }

    public void tick() {
        try {
            current--;
            update();
            if (current <= 0) {
                finish();
            }
        } catch (Exception ex) {
            Bukkit.getServer().getLogger().warning(String.format("timer %s tick error: %s", name, ex.getMessage()));
        }
    }

    /**
     * 计时器启动
     */
    protected void start() {
        try {
            listener.onTimerStart(new TimerEvent(this));
        } catch (Exception ex) {
            Bukkit.getServer().getLogger().warning(String.format("timer %s start error: %s", name, ex.getMessage()));
        }
    }

    protected void update() {
        listener.onTimerStart(new TimerEvent(this));
    }

    protected void finish() {
        listener.onTimerStart(new TimerEvent(this));
        try {
            close();
        } catch (Exception ex) {
            Bukkit.getServer().getLogger().warning(String.format("timer %s finish error: %s", name, ex.getMessage()));
        }
    }

    @Override
    public void close() throws Exception {

    }
}
