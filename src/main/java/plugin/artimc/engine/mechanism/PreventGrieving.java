package plugin.artimc.engine.mechanism;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerDropItemEvent;
import plugin.artimc.engine.IGame;
import plugin.artimc.engine.Mechanism;
import plugin.artimc.engine.timer.GameTimer;
import plugin.artimc.game.PvPGame;
import plugin.artimc.utils.GameUtil;

/**
 * 防止偷窃物品
 */
public class PreventGrieving extends Mechanism {

    private static int PERIOD = 5;

    public PreventGrieving(IGame game) {
        super(game);
    }

    /**
     * 1. 游戏进行中
     * 2. 剩余时间还是10秒
     * 3. 开启了物品统一
     *
     * @return
     */
    public boolean check() {
        PvPGame game = (PvPGame) getGame();
        return game.isGaming() && game.getPvpItemController() != null && game.getPvpItemController().isUnifiedInventory();
    }


    @Override
    public void onGamePeriodUpdate(GameTimer timer) {
        if (check() && getGame().getGameLeftTime() == PERIOD + 5)
            getGame().sendMessage(ChatColor.GRAY + "游戏即将结束，5秒后清理清理掉落物...");

        /**
         * 游戏即将结束，清理物品
         */
        if (check() && getGame().getGameLeftTime() < PERIOD)
            GameUtil.clearDropItems(getGame().getMap().getWorld());

        /**
         * 游戏刚开始，清理物品
         */
        if (check() && getGame().getGamePassedTime() <= 2)
            GameUtil.clearDropItems(getGame().getMap().getWorld());

        super.onGamePeriodUpdate(timer);
    }

    /**
     * 无法丢物品
     *
     * @param event
     */
    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (check() && getGame().getGameLeftTime() < PERIOD) {
            event.getPlayer().sendMessage(ChatColor.GRAY + "游戏即将结束，你无法丢弃物品");
            event.setCancelled(true);
        }
        super.onPlayerDropItem(event);
    }
}
