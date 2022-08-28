package plugin.artimc.engine;

/**
 * 游戏关闭原因
 */
public enum CloseReason {
    /**
     * 因没有等到竞争者而关闭
     */
    NO_COMPANION,

    /**
     * 由于没有在线玩家而关闭
     */
    NO_ONLINE_PLAYERS,

    /**
     * 因游戏正常结束而关闭
     */
    GAME_FINISH
}
