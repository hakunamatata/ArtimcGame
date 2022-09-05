package plugin.artimc.instance;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugin.artimc.ArtimcPlugin;
import plugin.artimc.engine.FinishReason;
import plugin.artimc.engine.mechanism.FillContainer;
import plugin.artimc.engine.timer.GameTimer;
import plugin.artimc.engine.Party;
import plugin.artimc.engine.event.GameItemPickupEvent;
import plugin.artimc.engine.timer.custom.ResourceDropTimer;
import plugin.artimc.game.PvPGame;
import plugin.artimc.utils.Utils;

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
    private int scoreMultiplier;

    public LogFactoryGame(String pvpGameName, ArtimcPlugin plugin) {
        super(pvpGameName, plugin);
        addMechanism(new FillContainer(this));
    }

    @Override
    protected void onInitialization() {
        setGameMap(new LogFactoryMap(getGameName(), getPlugin()));
        items = getGameMap().getResourceItems();
        resPeriod = getGameMap().getResourceTimerPeriod();
        resFreq = getGameMap().getResourceTimerFreq();
        scoreMultiplier = 1;
        super.onInitialization();
    }

    @Override
    public LogFactoryMap getGameMap() {
        return (LogFactoryMap) super.getGameMap();
    }

    public void dropItems() {
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
                    extra = Integer.parseInt(raw.split(";")[2]);
                } catch (Exception ex) {
                    extra = 0;
                }
                String mn = Utils.getRandomElement(Arrays.stream(mats.split(",")).toList());
                material = Material.valueOf(mn.split(":")[0].toUpperCase());
                amount = Integer.parseInt(mn.split(":")[1]);
                world = getPlugin().getServer().getWorld(getGameMap().getWorldName());
                loc = new Location(world, Integer.parseInt(lots.split(",")[0]), Integer.parseInt(lots.split(",")[1]), Integer.parseInt(lots.split(",")[2]));

                dropItem(new ItemStack(material, amount), loc).setItemMeta("extra-score", extra * scoreMultiplier);
                assert world != null;
                log(String.format("资源已生成：%s:%s, 地点：%s, %s %s %s", material, amount, world.getName(), loc.getX(), loc.getY(), loc.getZ()));
            }
            scoreMultiplier *= 2;
        } catch (Exception ex) {
            getPlugin().getLogger().warning(ex.getMessage());
        }
    }


    @Override
    public void onGamePeriodUpdate(GameTimer timer) {
        super.onGamePeriodUpdate(timer);
        // 每分钟新建一个计时器，生成资源
        if (isGaming() && getGamePassedTime() > 10 && getGameLeftTime() % resFreq == 0 && getGameLeftTime() > 0) {
            getTimerManager().startTimer(new ResourceDropTimer(resPeriod, "§6§l资源即将刷新", BarColor.BLUE, this));
        }
    }

    @Override
    protected void onGameFinish(FinishReason reason) {
        if (reason == FinishReason.GAMING_TIMEOUT) {
            giveExperienceReward();
        }
        super.onGameFinish(reason);
    }

    /**
     * 给予参与的玩家经验值奖励
     * 基础经验值 * 玩家数量 / 2
     */
    private void giveExperienceReward() {
        int players = getOnlinePlayersExceptObserver().size();
        int baseExp = getGameMap().getBaseExperience();
        int winnerExp = getGameMap().getWinnerExperience();
        // 经验值 * 玩家数量 / 2
        if (!getWinners().isEmpty()) {
            for (Party p : getWinners()) {
                for (Player player : p.getOnlinePlayers()) {
                    int exp = winnerExp * players / 2;
                    player.giveExp(exp);
                    player.sendMessage(getGameLocaleString("experience-acquired").replace("%exp%", String.valueOf(exp)));
                }
            }
        }
        if (!getLosers().isEmpty()) {
            for (Party p : getLosers()) {
                for (Player player : p.getOnlinePlayers()) {
                    int exp = baseExp * players / 2;
                    player.giveExp(exp);
                    player.sendMessage(getGameLocaleString("experience-acquired").replace("%exp%", String.valueOf(exp)));
                }
            }
        }
    }


    @Override
    public void onGameItemPickup(GameItemPickupEvent event) {
        if (
            // 对Ob无效
                !getObserveParty().contains(event.getPlayer()) &&
                        // 对观察者无效
                        !event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)
        ) {
            int score = (int) event.getItem().getItemMeta("extra-score");
            Party party = getManager().getPlayerParty(event.getPlayer());
            getPvPSStatistic().addPartyScore(party, score);
            String message = ChatColor.translateAlternateColorCodes('&', getGameMap().getConfig().getString("translate.item-pickup").replace("%party_name%", party.getName()).replace("%score%", String.valueOf(score)));
            sendMessage(message);
        } else {
            event.setCancel(true);
        }
        super.onGameItemPickup(event);
    }
}
