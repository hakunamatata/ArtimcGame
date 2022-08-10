package plugin.artimc.engine;

public enum GameFinishReason {
  /**
   * 比赛时间结束
   */
  GAMING_TIMEOUT,
  /**
   * 没有对手加入
   */
  NO_COMPANION,
  /**
   * 游戏过程中，对手中途离场
   */
  MISSING_COMPANION
}
