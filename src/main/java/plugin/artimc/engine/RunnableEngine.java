package plugin.artimc.engine;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.artimc.engine.timer.Manager;

/**
 * RunnableEngine
 * 描述：处理游戏帧的运行逻辑
 * 作者：Leo
 * 创建时间：2022/08/07 22:34
 */
public abstract class RunnableEngine extends BukkitRunnable implements AutoCloseable {
    Plugin plugin;

    private int currentTick = 0;

    private Manager timerManager;

    public RunnableEngine(Plugin plugin) {
        this.plugin = plugin;
        timerManager = new Manager();
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public Manager getTimerManager() {
        return timerManager;
    }

    protected abstract void onUpdate();

    protected abstract void onFixedUpdate();

    @Override
    public void run() {
        try {
            // 每帧运行
            onUpdate();
            if (currentTick % 20 == 0) {
                // 每秒运行
                onFixedUpdate();
                timerManager.tick();
            }
        }
        // 不能让异常中断游戏
        catch (Exception ex) {
            plugin.getLogger().warning("tick error: " + ex.getMessage());
        }
        // 无论如何，游戏时间刻都在增加
        finally {
            currentTick++;
        }
    }

    @Override
    public void close() throws Exception {

    }
}
