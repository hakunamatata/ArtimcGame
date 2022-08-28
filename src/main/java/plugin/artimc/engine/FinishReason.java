package plugin.artimc.engine;

public enum FinishReason {
    /**
     * 比赛时间结束
     */
    GAMING_TIMEOUT,
    /**
     * 游戏过程中，对手中途离场
     */
    MISSING_COMPANION
}
