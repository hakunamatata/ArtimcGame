package plugin.artimc.engine;

import org.bukkit.plugin.Plugin;
import plugin.artimc.engine.timer.Timer;
import plugin.artimc.engine.timer.TimerEvent;

/**
 * GameEngine
 * 描述：游戏引擎，约定：游戏状态、游戏开始、游戏结束、游戏地图
 * 以 GameEngine 为模板，允许创建 简单的小游戏，PVE 游戏等
 * 作者：Leo
 * 创建时间：2022/08/11 22:34
 */
public abstract class GameEngine extends RunnableEngine {

    private GameStatus gameStatus;

    public GameEngine(Plugin plugin) {
        super(plugin);
        // init fields
        // ...
        gameStatus = GameStatus.CREATE;
    }

    /**
     * 游戏状态管理：
     * - 等待中，表示等待玩家加入，满足游戏开始条件之后，立即开始游戏，并自动停止等待计时器
     * - 进行中，表示游戏正在进行，满足游戏结束条件之后，立即结束游戏，并自动停止进行计时器
     * - 结算中，表示游戏已经结束，向游戏内的玩家发送游戏结果，倒计时结束之后，自动释放游戏
     */

    @Override
    public void onTimerStart(TimerEvent event) {

    }

    @Override
    public void onTimerTick(TimerEvent event) {

    }

    @Override
    public void onTimerFinish(TimerEvent event) {

    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onFixedUpdate() {

    }
}
