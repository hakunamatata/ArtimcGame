package plugin.artimc.engine;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * IGamePlayer
 * 描述：玩家控制器接口
 * 对游戏中的 玩家、队伍 的管理
 * 作者：Leo
 * 创建时间：2022/08/07 22:34
 */
public interface IGamePlayer {

    /**
     * 游戏内玩家
     *
     * @return
     */
    Set<UUID> getPlayers();

    /**
     * 游戏内队伍
     *
     * @return
     */
    Map<PartyName, Party> getParties();


}
