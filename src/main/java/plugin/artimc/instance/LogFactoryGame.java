package plugin.artimc.instance;

import org.bukkit.plugin.Plugin;
import plugin.artimc.game.PvPGame;

/**
 * LogFactoryGame
 * 描述：木材工厂游戏，队伍配置：5v5; 资源地图，生产木材、绿宝石、钻石、极小概率出下界合金
 * 作者：Leo
 * 创建时间：2022/08/09 22:34
 */
public class LogFactoryGame extends PvPGame {

    public LogFactoryGame(String pvpGameName, Plugin plugin) {
        super(pvpGameName, plugin);
    }
}
