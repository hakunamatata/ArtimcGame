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
    private Manager manager;
    private TimerState state;

    private TimerListener listener;

    public Timer(String name, int period, Manager manager) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.period = period;
        this.current = period;
        this.manager = manager;
        this.state = TimerState.CREATED;
    }

    public void addTimerListener(TimerListener listener){
        this.listener = listener;
    }

    public Timer(int period, Manager manager) {
        this.uuid = UUID.randomUUID();
        this.name = this.uuid.toString();
        this.period = period;
        this.current = period;
        this.manager = manager;
        this.state = TimerState.CREATED;
    }

    public TimerState getState() {
        return state;
    }


    public void tick() {
        try {
            current--;
            update();
            if (current <= 0 && state == TimerState.UPDATE) {
                finish();
                close();
            }
        } catch (Exception ex) {
            Bukkit.getServer().getLogger().warning(String.format("timer %s tick error: %s", name, ex.getMessage()));
        }
    }

    /**
     * 计时器启动
     * 向计时器管理器中添加自己
     * tick 统一交由管理器处理
     */
    public void start() {
        manager.getTimers().put(name, this);
        state = TimerState.START;
        onStart();
    }

    protected abstract void onStart();

    private void update() {
        state = TimerState.UPDATE;
        onUpdate();
    }

    protected abstract void onUpdate();

    private void finish() {
        state = TimerState.FINISH;
        onFinish();
    }

    /**
     * 计时器时间到之后，将自己从管理器中移除
     */
    protected void onFinish() {
        manager.getTimers().remove(name);
    }

    @Override
    public void close() throws Exception {

    }
}
