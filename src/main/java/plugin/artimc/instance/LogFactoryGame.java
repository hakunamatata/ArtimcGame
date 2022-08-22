package plugin.artimc.instance;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import plugin.artimc.ArtimcGamePlugin;
import plugin.artimc.engine.GameFinishReason;
import plugin.artimc.engine.GameStatus;
import plugin.artimc.engine.GameTimer;
import plugin.artimc.engine.Party;
import plugin.artimc.engine.event.GameItemPickupEvent;
import plugin.artimc.game.PvPGame;
import plugin.artimc.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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

    private int scoreMultipler;

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
        scoreMultipler = 1;
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
                int extra;
                try {
                    extra = Integer.valueOf(raw.split(";")[2]);
                } catch (Exception ex) {
                    extra = 0;
                }
                String mn = Utils.getRandomElement(Arrays.stream(mats.split(",")).toList());
                material = Material.valueOf(mn.split(":")[0].toUpperCase());
                amount = Integer.valueOf(mn.split(":")[1]);
                world = getPlugin().getServer().getWorld(map.getWorldName());
                loc = new Location(world, Integer.valueOf(lots.split(",")[0]), Integer.valueOf(lots.split(",")[1]), Integer.valueOf(lots.split(",")[2]));

                dropItem(new ItemStack(material, amount), loc).setItemMeta("extra-score", extra * scoreMultipler);
                //world.dropItem(loc, new ItemStack(material, amount));
                log(String.format("资源已生成：%s:%s, 地点：%s, %s %s %s", material, amount, world.getName(), loc.getX(), loc.getY(), loc.getZ()));
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
                    if (getGameStatus() == GameStatus.GAMING) {
                        setEnableStatusBar(false);
                    }
                    super.onStart();
                }

                @Override
                protected void onUpdate() {
                    // 设置资源倒计时
                    getStatusBar().setTitle("§6§l资源即将刷新");
                    getStatusBar().setColor(BarColor.BLUE);
                    getStatusBar().setProgress((double) this.getCurrent() / (double) this.getPeriod());
                    super.onUpdate();
                }

                @Override
                protected void onFinish() {
                    // 生成资源
                    if (getGameStatus() == GameStatus.GAMING) {
                        setEnableStatusBar(true);
                        dropItems();
                        scoreMultipler *= 2;
                    }
                    super.onFinish();
                }
            }.start();
        }

        if (isEnableStatusBar()) super.onGameTimerUpdate(timer);
    }

    @Override
    protected void onGameFinish(GameFinishReason reason) {
        if (reason == GameFinishReason.GAMING_TIMEOUT) {
            giveExperienceReward();
        }
        if (getTimerManager().get("resource-drop") != null) {
            getTimerManager().get("resource-drop").close();
        }
        super.onGameFinish(reason);
    }

    /**
     * 给予参与的玩家经验值奖励
     * 基础经验值 * 玩家数量 / 2
     */
    private void giveExperienceReward() {
        int players = getOnlinePlayersExceptObserver().size();
        int baseExp = map.getBaseExperience();
        int winnerExp = map.getWinnerExperience();
        // 经验值 * 玩家数量 / 2
        if (!getWinners().isEmpty()) {
            for (Party p : getWinners()) {
                for (Player player : p.getOnlinePlayers()) {
                    int exp = winnerExp * players / 2;
                    player.giveExp(exp);
                    player.sendMessage(getGameLocaleString("experience-acquired")
                            .replace("%exp%", String.valueOf(exp)));
                }
            }
        }
        if (!getLosers().isEmpty()) {
            for (Party p : getLosers()) {
                for (Player player : p.getOnlinePlayers()) {
                    int exp = baseExp * players / 2;
                    player.giveExp(exp);
                    player.sendMessage(getGameLocaleString("experience-acquired")
                            .replace("%exp%", String.valueOf(exp)));
                }
            }
        }
    }


    @Override
    public void onGameItemPickup(GameItemPickupEvent event) {
        // 对Ob无效
        if (!getObserveParty().contains(event.getPlayer())) {
            int score = (int) event.getItem().getItemMeta("extra-score");
            Party party = getManager().getPlayerParty(event.getPlayer());
            getPvPStatstic().addPartyScore(party, score);
            String message = ChatColor.translateAlternateColorCodes('&', getGameMap().getConfig().getString("translate.item-pickup")
                    .replace("%party_name%", party.getName())
                    .replace("%score%", String.valueOf(score)));
            sendMessage(message);
        } else {
            event.setCancel(true);
        }
        super.onGameItemPickup(event);
    }
}
