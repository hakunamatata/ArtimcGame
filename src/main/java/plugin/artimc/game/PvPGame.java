package plugin.artimc.game;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import org.jetbrains.annotations.NotNull;
import plugin.artimc.ArtimcPlugin;
import plugin.artimc.engine.*;
import plugin.artimc.engine.event.GameItemPickupEvent;
import plugin.artimc.engine.event.PartyJoinGameEvent;
import plugin.artimc.engine.event.PartyLeaveGameEvent;
import plugin.artimc.engine.event.PlayerLeaveGameEvent;
import plugin.artimc.engine.mechanism.PreventGrieving;
import plugin.artimc.engine.timer.GameTimer;
import plugin.artimc.engine.timer.effect.PvPGameInvincibleEffect;
import plugin.artimc.scoreboard.GameScoreboard;
import plugin.artimc.utils.StringUtil;

/**
 * PvPGame:
 * 游戏开始需要双方队长准备就绪
 */
public class PvPGame extends Game {

    private final Map<PartyName, Boolean> readyStatus = new EnumMap<>(PartyName.class);
    private PartyName hostPartyName;
    private PartyName guestPartyName;
    private PvPStatstic pvpStatstic;
    private PvPItemControl pvpItemController;
    private String gameMode;

    public PvPGame(String pvpGameName, ArtimcPlugin plugin) {
        super(pvpGameName, plugin);
        // 使用队伍命名牌显示策略
        setNameTagManager(new PartyNameTagManager(this));
        // 开启防盗模式
        useMechanisms(new PreventGrieving(this));
    }

    @Override
    protected void onInitialization() {
        pvpStatstic = new PvPStatstic(this);
        pvpItemController = new PvPItemControl(this);
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public List<String> getAvaliableGameModes() {
        return getPlugin().getGameConfigurations().get(getGameName()).getStringList("modes.list");
    }


    public PvPItemControl getPvpItemController() {
        return pvpItemController;
    }

    /**
     * 将玩家移动到指定队伍
     *
     * @param player
     * @param party
     */
    public void movePlayer(@NotNull Player player, Party party) {
        getCompanion().movePlayer(player, party);
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onFixedUpdate() {
        if (isGaming() && pvpItemController != null) {
            pvpItemController.replaceItems();
        }
        super.onFixedUpdate();
    }


    private void clearDropItemsOnUnifiedMode() {
        if (pvpItemController != null && pvpItemController.isUnifiedInventory()) {

        }
    }

    @Override
    protected void onGameFinish(FinishReason reason) {
        super.onGameFinish(reason);

        // 结束后恢复玩家的物品
        if (pvpItemController != null) {
            pvpItemController.recoverItems();
        }
        // 对方队伍成员均离线
        if (reason == FinishReason.MISSING_COMPANION) {
            Party party = null;
            if (getHostParty() != null && getHostParty().getOnlinePlayers().isEmpty()) party = getHostParty();
            else if (getGuestParty() != null && getGuestParty().getOnlinePlayers().isEmpty()) party = getGuestParty();
            if (party != null)
                sendMessage(getLocaleString("game.game-finish-reason-missing-companion", false).replace("%party_name%", party.getName()));
        }
        // 比赛时间结束
        else if (reason == FinishReason.GAMING_TIMEOUT) {
            for (Party party : this.getGameParties().values()) {
                // 队内广播，队伍数据
                sendMessage(getPvPSStatistic().getPartySummary(party));
                for (Player player : party.getOnlinePlayers()) {
                    // 向玩家发送玩家数据
                    player.sendMessage(getPvPSStatistic().getPlayerSummary(player));
                }
            }
        }

        for (Party party : getWinners()) {
            party.showTitle(this.getGameMap().getWinTitle(), this.getGameMap().getWinSubTitle());
            party.sendMessage(ChatColor.GREEN + this.getGameMap().getWinTitle());
        }

        for (Party party : getLosers()) {
            party.showTitle(this.getGameMap().getLooseTitle(), this.getGameMap().getLooseSubTitle());
            party.sendMessage(ChatColor.GREEN + this.getGameMap().getLooseTitle());
        }

    }

    /**
     * 设置游戏的胜利条件
     *
     * @param party
     * @return
     */
    @Override
    protected boolean willWinTheGame(Party party) {
        /**
         * 按照当前游戏的规则，将造成伤害最高的队伍判定为胜利的一方
         * 伤害 * 击杀数 最高作为 胜利条件
         */
        if (pvpStatstic != null) {
            return party.equals(pvpStatstic.getMvpParty());
        }
        return false;
    }

    public PvPStatstic getPvPSStatistic() {
        return pvpStatstic;
    }

    /**
     * 获取主队
     *
     * @return
     */
    public Party getHostParty() {
        if (hostPartyName == null) throw new IllegalStateException(getGameLocaleString("host-party-not-ready"));

        return getGameParties().get(hostPartyName);
    }

    /**
     * 获取客队
     *
     * @return
     */
    public Party getGuestParty() {
        if (guestPartyName == null)  //throw new IllegalStateException(getGameLocaleString("guest-party-not-ready"));
            return null;

        return getGameParties().get(guestPartyName);
    }

    /**
     * 主队是否准备就绪
     *
     * @return
     */
    public boolean isHostReady() {
        if (hostPartyName == null) return false;
        return readyStatus.get(hostPartyName);
    }

    /**
     * 主队是否准备就绪
     *
     * @return
     */
    public boolean isGuestReady() {
        if (guestPartyName == null) return false;
        return readyStatus.get(guestPartyName);
    }

    /**
     * 队伍准备
     *
     * @param party
     * @param ready
     */
    public void setPartyReady(Party party, boolean ready) {
        if (readyStatus.containsKey(party.getPartyName())) {
            readyStatus.put(party.getPartyName(), ready);
            if (ready) {
                sendMessage(getGameLocaleString("party-is-ready", false).replace("%party_name%", party.getName()));
            } else {
                sendMessage(getGameLocaleString("party-is-unready", false).replace("%party_name%", party.getName()));
            }
        } else {
            party.sendMessage(getGameLocaleString("ur-party-is-not-in-game"));
        }
    }

    @Override
    public void onPartyJoinGame(PartyJoinGameEvent event) {
        Party party = event.getParty();
        if (getGameParties().size() == 1) {
            hostPartyName = party.getPartyName();
        } else if (getGameParties().size() == 2) {
            guestPartyName = party.getPartyName();
        }
        readyStatus.putIfAbsent(party.getPartyName(), false);
        if (party.equals(getHostParty())) {
            // 加入了主队
            party.getOwner().sendMessage(getGameLocaleString("ur-party-is-host"));

        } else if (party.equals(getGuestParty())) {
            // 加入了客队
            party.getOwner().sendMessage(getGameLocaleString("ur-party-is-guest"));
        }

        super.onPartyJoinGame(event);
    }


    /**
     * 队伍离开
     *
     * @param event
     */
    @Override
    public void onPartyLeaveGame(PartyLeaveGameEvent event) {

        try {
            Party hostParty = getHostParty();
            Party guestParty = getGuestParty();
            // 主队离开
            if (event.getParty().equals(hostParty)) {
                if (guestParty != null) {
                    // 将主队设置为现有的客队
                    hostPartyName = guestParty.getPartyName();
                    // 将现有的客队设置为空
                    guestPartyName = null;
                }
            }
            // 客队离开
            else if (event.getParty().equals(guestParty)) {
                guestPartyName = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onPartyLeaveGame(event);
    }


    @Override
    public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
        if (event.getPlayer().isOnline()) {
            Player player = (Player) event.getPlayer();
            // 初始化玩家状态
            player.setGlowing(false);
            player.setHealthScaled(false);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            if (pvpItemController != null) {
                pvpItemController.recoverItemForPlayer(player);
            }
        }
        super.onPlayerLeaveGame(event);
    }


    @Override
    protected void onGameStart() {
        for (Party party : getGameParties().values()) {
            if (party.equals(getHostParty())) {
                party.teleport(getGameMap().getSpawn().get("host"));
            } else if (party.equals(getGuestParty())) {
                party.teleport(getGameMap().getSpawn().get("guest"));
            }

            // 初始化玩家状态
            for (Player player : party.getOnlinePlayers()) {
                player.setGlowing(false);
                player.setHealthScaled(false);
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
            }
        }
        showTitle(getGameMap().getStartTitle(), getGameMap().getStartSubTitle(), 20, 100, 20);
        super.onGameStart();
    }

    @Override
    protected Class<? extends GameScoreboard> useScoreboard() {
        return PvPGameScoreboard.class;
    }

    /**
     * 游戏开始条件
     */
    @Override
    protected boolean willGameStart() {
        // 所有队伍准备就绪
        // 游戏自动开始
        if (getGameStatus().equals(GameStatus.WAITING) && getGameParties().size() == 2 && getGameParties().size() == readyStatus.size()) {
            return !readyStatus.values().contains(false);
        }
        return false;
    }

    /**
     * 游戏结束原因
     */
    @Override
    protected FinishReason willGameFinish() {
        // 如果游戏过程中，中途任意一支队伍玩家数量为 0 ，比赛终止
        if ((Party.isEmpty(getHostParty()) || Party.isEmpty(getGuestParty())) && isGaming())
            return FinishReason.MISSING_COMPANION;

        return super.willGameFinish();
    }

    /**
     * 玩家是否承受伤害
     * 如果不承受伤害，则不会对玩家造成伤害
     */
    @Override
    protected boolean willPlayerTakesDamage(Player player, Player damager) {
        // 游戏还没有真正开始，玩家不承受伤害
        if (!isGaming()) {
            return false;
        } else {
            Party party = getManager().getPlayerParty(player);
            if (party != null && party.contains(damager) && !isFriendlyFire()) {
                // 同在一个队伍中, 且关闭了友方误伤
                return false;
            }
        }
        return super.willPlayerTakesDamage(player, damager);
    }


    @Override
    public void onWaitPeriodUpdate(GameTimer timer) {
        if (getStatusBar().isSyncDefaultStatusBar()) {
            if (getGameParties().size() == 1) {
                getStatusBar().setTitle(getGameLocaleString("status-title-waiting-party", false));
                getStatusBar().setColor(BarColor.RED);
            } else if (getGameParties().size() == 2) {
                getStatusBar().setTitle(getGameLocaleString("status-title-waiting-ready", false));
                getStatusBar().setColor(BarColor.YELLOW);
            }
        }
        super.onWaitPeriodUpdate(timer);
    }

    @Override
    public void onGamePeriodUpdate(GameTimer timer) {
        if (getStatusBar().isSyncDefaultStatusBar()) {
            if (timer.getPassedTime() < 5) {
                getStatusBar().setColor(BarColor.GREEN);
                getStatusBar().setTitle(getGameLocaleString("status-title-gaming", false));
            } else if (timer.getCurrent() > (timer.getPeriod() / 2)) {
                getStatusBar().setColor(BarColor.GREEN);
                getStatusBar().setTitle("§a§l" + getGameLocaleString("status-title-time-left", false).replace("%time%", StringUtil.formatTime(timer.getCurrent())));
            } else if (timer.getCurrent() > (timer.getPeriod() / 5)) {
                getStatusBar().setColor(BarColor.YELLOW);
                getStatusBar().setTitle("§e§l" + getGameLocaleString("status-title-time-left", false).replace("%time%", StringUtil.formatTime(timer.getCurrent())));
            } else if (timer.getCurrent() > (timer.getPeriod() / 10)) {
                getStatusBar().setColor(BarColor.RED);
                getStatusBar().setTitle("§c§l" + getGameLocaleString("status-title-time-left", false).replace("%time%", StringUtil.formatTime(timer.getCurrent())));
            } else {
                getStatusBar().setColor(BarColor.RED);
                getStatusBar().setTitle("§4§l" + getGameLocaleString("status-title-time-left", false).replace("%time%", StringUtil.formatTime(timer.getCurrent())));
            }
        }

        super.onGamePeriodUpdate(timer);
    }

    @Override
    public void onFinishPeriodUpdate(GameTimer timer) {
        if (getStatusBar().isSyncDefaultStatusBar()) {
            getStatusBar().setTitle(getGameLocaleString("status-title-game-finish", false));
            getStatusBar().setColor(BarColor.BLUE);
        }
        super.onFinishPeriodUpdate(timer);
    }


    @Override
    public Party inSpawnProtection(Player player) {
        double protRange = getGameMap().getSpawnProtectionRange();

        try {
            if (getHostParty() != null && getGameMap().getSpawn().get("host").distance(player.getLocation()) < protRange) {
                return getHostParty();
            }

            if (getGuestParty() != null && getGameMap().getSpawn().get("guest").distance(player.getLocation()) < protRange) {
                return getGuestParty();
            }
        } catch (Exception ex) {

        }

        return super.inSpawnProtection(player);
    }

    @Override
    public Location getRespawnLocation(Player player) {
        if (getHostParty().contains(player)) {
            return getMap().getSpawn().get("host");
        }

        if (getGuestParty().contains(player)) {
            return getMap().getSpawn().get("guest");
        }

        return null;
    }

    //
//    @Override
//    public void onPlayerRespawn(PlayerRespawnEvent event) {
//        int period = getGameMap().getInvinciblePeriod();
//        Player player = event.getPlayer();
//        if (getHostParty().contains(player)) {
//            event.setRespawnLocation(getGameMap().getSpawn().get("host"));
//            givePlayerEffect(new PvPGameInvincibleEffect(player, period, this));
//        } else if (getGuestParty().contains(player)) {
//            event.setRespawnLocation(getGameMap().getSpawn().get("guest"));
//            givePlayerEffect(new PvPGameInvincibleEffect(player, period, this));
//        } else event.setRespawnLocation(getGameMap().getSpawn().get("default"));
//        super.onPlayerRespawn(event);
//    }

    @Override
    public void onPlayerDamageByPlayer(Player player, Player damager, EntityDamageByEntityEvent eventSource) {

        Party inWitchSpawnParty;
        inWitchSpawnParty = inSpawnProtection(player);
        // 玩家在己方保护点收到伤害
        if (inWitchSpawnParty != null && inWitchSpawnParty.contains(player)) {
            damager.sendMessage(getGameLocaleString("too-near-to-spawn"));
            eventSource.setCancelled(true);
        }

        inWitchSpawnParty = inSpawnProtection(damager);
        // 玩家在己方保护点内攻击其他玩家
        if (inWitchSpawnParty != null && inWitchSpawnParty.contains(damager)) {
            damager.sendMessage(getGameLocaleString("too-near-to-spawn"));
            eventSource.setCancelled(true);
        }
        if (pvpStatstic != null && isGaming() && !eventSource.isCancelled()) {
            pvpStatstic.onAttack(getCurrentTick(), damager, player, eventSource.getDamage());
        }

        super.onPlayerDamageByPlayer(player, damager, eventSource);
    }

    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (isGaming() && pvpStatstic != null) {
            pvpStatstic.onDeath(getCurrentTick(), event.getPlayer(), event.getPlayer().getKiller());
        }
        super.onPlayerDeath(event);
    }

    /**
     * 玩家上线，但还在游戏中
     */
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        /**
         * xxx
         */
        super.onPlayerJoin(event);
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (pvpItemController != null) {
            pvpItemController.recoverItemForPlayer(event.getPlayer());
        }
        super.onPlayerQuit(event);
    }


    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        // 玩家收到了伤害
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Party inWitchSpawnParty = inSpawnProtection(player);
            // 表示玩家在自己的出生点收到伤害，一律无视
            if (inWitchSpawnParty != null && inWitchSpawnParty.contains(player)) {
                event.setCancelled(true);
            }
        }
        super.onPlayerDamage(event);
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        Party inWitchSpawnParty = inSpawnProtection(player);
        // 表示玩家在其他队伍的出生点附近 放置方块等
        if (inWitchSpawnParty != null && !inWitchSpawnParty.contains(player)) {
            player.sendMessage(getGameLocaleString("too-near-to-spawn"));
            event.setCancelled(true);
        }

        super.onBlockPlace(event);
    }

    @Override
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Party inWitchSpawnParty = inSpawnProtection(player);
        // 表示玩家在其他队伍的出生点附近 放岩浆水桶等
        if (inWitchSpawnParty != null && !inWitchSpawnParty.contains(player)) {
            player.sendMessage(getGameLocaleString("too-near-to-spawn"));
            event.setCancelled(true);
        }
        super.onPlayerBucketEmpty(event);
    }

    @Override
    public void onGameItemPickup(GameItemPickupEvent event) {
        super.onGameItemPickup(event);
    }

    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        // 游戏已结束，或者游戏时间还剩60秒
        // 玩家无法丢弃物品
        if (pvpItemController != null && pvpItemController.isUnifiedInventory() && (getGameStatus() == GameStatus.FINISH || (getGameStatus() == GameStatus.GAMING && getGameLeftTime() <= 10))) {
            event.setCancelled(false);
        }
        super.onPlayerDropItem(event);
    }
}
