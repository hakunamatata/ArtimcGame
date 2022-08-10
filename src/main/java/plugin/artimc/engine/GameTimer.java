package plugin.artimc.engine;

import java.util.UUID;

/**
 * 游戏内的各种倒计时功能
 */
public abstract class GameTimer {
    private UUID uuid;
    /**
     * 这是计时器的名称
     */
    private String name;

    private int period;

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    private int current;

    public Game getGame() {
        return game;
    }


    private Game game;

    public GameTimer(String name, int period, Game game) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.period = period;
        this.current = period;
        this.game = game;
    }

    public GameTimer(int period, Game game) {
        this.uuid = UUID.randomUUID();
        this.name = this.uuid.toString();
        this.period = period;
        this.current = period;
        this.game = game;
    }

    public String getName() {
        return name;
    }

    public int getCurrent() {
        return current;
    }

    public int getPeriod() {
        return period;
    }

    /**
     * 计时器停止，调用结束事假
     */
    public void stop() {
        try {
            onFinish();
            close();
        } catch (Exception ex) {
            game.getPlugin().getLogger().warning(ex.getMessage());
        }
    }

    /**
     * 强制关闭计时器
     */
    public void close() {
        game.getTimerManager().remove(name);
    }


    public void tick() {
        try {
            this.onUpdate();
            current--;
            if (current < 0) current = 0;
        } catch (Exception ex) {
            game.getPlugin().getLogger().warning(ex.getMessage());
        }
    }

    public void start() {
        onStart();
    }

    /**
     * 干！！！
     */
    protected void onStart() {
        game.getTimerManager().put(name, this);
    }


    protected void onFinish() {
    }

    protected void onUpdate() {
    }

}
