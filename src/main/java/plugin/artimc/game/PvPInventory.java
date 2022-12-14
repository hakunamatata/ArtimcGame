package plugin.artimc.game;

import org.bukkit.entity.Player;

/**
 * 描述：PvPInventory，背包控制；
 * 控制游戏过程中，玩家背包中的物品。 统一玩家背包、玩家实际的背包保存至 数据库，游戏结束之后复原
 * ----------------------------------------------
 * 根据背包管理中定义的类别进行统一，管理指令添加背包控制：
 * 例如： /game 设置 背包 #类别 #管理当前背包
 * ----------------------------------------------
 * 使用统一背包，游戏结束之后复原， 或者玩家中途离场复原
 * <p>
 * 作者：Leo
 * 创建时间：2022/7/29 20:40
 * 更新时间：2022/8/15 09:10
 */
public class PvPInventory {

    /**
     * 保存物品栏
     *
     * @param player
     */
    public void saveInventory(Player player) {

    }

    /**
     * 恢复物品栏
     *
     * @param player
     */
    public void recoverInventory(Player player) {
    }

}
