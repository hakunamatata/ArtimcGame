package plugin.artimc.instance;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import plugin.artimc.ArtimcGamePlugin;
import plugin.artimc.engine.GameTimer;
import plugin.artimc.game.PvPGame;
import plugin.artimc.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LogFactoryGame
 * 描述：木材工厂游戏，队伍配置：5v5; 资源地图，生产木材、绿宝石、钻石、极小概率出下界合金
 * 作者：Leo
 * 创建时间：2022/08/09 22:34
 */
public class LogFactoryGame extends PvPGame {

    private int resPeriod;
    private int resFreq;
    private List<String> items;
    private LogFactoryMap map;

    public LogFactoryGame(String pvpGameName, Plugin plugin) {
        super(pvpGameName, plugin);
    }

    @Override
    protected void onInitialization() {
        final ArtimcGamePlugin agp = getPlugin();
        map = new LogFactoryMap(agp.getGameConfigurations().get(getGameName()), getPlugin());
        items = map.getResourceItems();
        resPeriod = map.getResourceTimerPeriod();
        resFreq = map.getResourceTimerFreq();
        super.onInitialization();
    }

    private void dropItems() {
        Material material;
        World world;
        Location loc;
        int amount;
        try {
            for (String raw : items) {
                //raw: apple:2,diamond:1;x,y,z
                String mats = raw.split(";")[0];
                String lots = raw.split(";")[1];
                String mn = Utils.getRandomElement(Arrays.stream(mats.split(",")).toList());
                material = Material.valueOf(mn.split(":")[0].toUpperCase());
                amount = Integer.valueOf(mn.split(":")[1]);
                world = getPlugin().getServer().getWorld(map.getWorldName());
                loc = new Location(world,
                        Integer.valueOf(lots.split(",")[0]),
                        Integer.valueOf(lots.split(",")[1]),
                        Integer.valueOf(lots.split(",")[2])
                );
                world.dropItem(loc, new ItemStack(material, amount));
                log(String.format("资源已生成：%s:%s, 地点：%s, %s %s %s",
                        material, amount, world.getName(),
                        loc.getX(), loc.getY(), loc.getZ()));
            }
        } catch (Exception ex) {
            getPlugin().getLogger().warning(ex.getMessage());
        }
    }


    @Override
    protected void onGameTimerUpdate(GameTimer timer) {

        // 每分钟新建一个计时器，生成资源
        if (getGamingLifeTime() > 30 && getGamingLifeTime() % resFreq == 0) {
            // 10秒倒计时，标题提醒，资源即将生成
            new GameTimer("resource-drop", resPeriod, this) {
                @Override
                protected void onStart() {
                    // 隐藏默认标题
                    setEnableStatusBar(false);
                    super.onStart();
                }

                @Override
                protected void onUpdate() {
                    // 设置资源倒计时
                    getStatusBar().setTitle("§6§l资源即将生成");
                    getStatusBar().setColor(BarColor.BLUE);
                    getStatusBar().setProgress((double) this.getCurrent() / (double) this.getPeriod());
                    super.onUpdate();
                }

                @Override
                protected void onFinish() {
                    // 生成资源
                    setEnableStatusBar(true);
                    dropItems();
                    super.onFinish();
                }
            }.start();
        }

        if (isEnableStatusBar()) super.onGameTimerUpdate(timer);
    }
}
