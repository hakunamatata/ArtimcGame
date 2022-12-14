package plugin.artimc.engine;

public enum GameStatus {
    // 默认状态
    // 实例刚创建完的状态
    INITIALIZED,
    // 等待状态，等待房主开始游戏
    WAITING,
    // 游戏中
    GAMING,
    // 结算中
    FINISH,
    // 关闭中
    CLOSING
}
