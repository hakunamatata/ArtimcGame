package plugin.artimc.game;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

import plugin.artimc.engine.*;
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

    public PvPGame(String pvpGameName, Plugin plugin) {
        super(pvpGameName, plugin);
    }

    @Override
    protected void onInitialization() {
        pvpStatstic = new PvPStatstic(this);
        pvpItemController = new PvPItemControl(this);
    }

    public PvPItemControl getPvpItemController() {
        return pvpItemController;
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onFixedUpdate() {
        if (isGaming() && pvpItemController != null) {
            pvpItemController.replaceItems();
        }

        // debug shit
        for (GameTimer timer : getTimerManager().values()) {
            if (timer instanceof GameBuffEffect) {
                log(String.format("------- timer: %s --------", ((GameBuffEffect) timer).getBuffName()));
                log(String.format(" Player: %s, period: %s, current: %s", ((GameBuffEffect) timer).getPlayer().getName(), timer.getPeriod(), timer.getCurrent()));
            }
        }
//        log(String.format(" "));
//        for (Set<GameBuffEffect> effects : getGameBuffEffects().values()) {
//            for (GameBuffEffect buff : effects) {
//                log(String.format("------- buff: %s --------", buff.getName()));
//                log(String.format(" Player: %s, period: %s, current: %s", buff.getPlayer().getName(), buff.getPeriod(), buff.getCurrent()));
//            }+
//        }

        super.onFixedUpdate();
    }

    @Override
    protected void onGameFinish(GameFinishReason reason) {
        // 结束后恢复玩家的物品
        if (pvpItemController != null) {
            pvpItemController.recoverItems();
        }
        // 对方队伍成员均离线
        if (reason == GameFinishReason.MISSING_COMPANION) {
            Party party = null;
            if (getHostParty().getOnlinePlayers().isEmpty()) party = getHostParty();
            else if (getGuestParty().getOnlinePlayers().isEmpty()) party = getGuestParty();
            if (party != null)
                sendMessage(getLocaleString("game.game-finish-reason-missing-companion", false).replace("%party_name%", party.getName()));
        }
        // 比赛时间结束
        else if (reason == GameFinishReason.GAMING_TIMEOUT) {
            for (Party party : this.getGameParties().values()) {
                // 队内广播，队伍数据
                sendMessage(getPvPStatstic().getPartySummary(party));
                for (Player player : party.getOnlinePlayers()) {
                    // 向玩家发送玩家数据
                    player.sendMessage(getPvPStatstic().getPlayerSummary(player));
                }
            }
        }

        for (Party party : getWinners()) {
            party.showTitle(this.getGameMap().getWinTitle(), this.getGameMap().getWinSubTitle());
        }

        for (Party party : getLosers()) {
            party.showTitle(this.getGameMap().getLooseTitle(), this.getGameMap().getLooseSubTitle());
        }

        super.onGameFinish(reason);
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

    public PvPStatstic getPvPStatstic() {
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
        if (guestPartyName == null) throw new IllegalStateException(getGameLocaleString("guest-party-not-ready"));

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
    protected void onPartyJoinGame(Party party) {
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
        super.onPartyJoinGame(party);
    }


    @Override
    protected void onPlayerLeaveGame(Player player) {
        // 初始化玩家状态
        player.setGlowing(false);
        player.setHealthScaled(false);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        if (pvpItemController != null) {
            pvpItemController.recoverItemForPlayer(player);
        }
        super.onPlayerLeaveGame(player);
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
    protected GameFinishReason willGameFinish() {
        // 如果游戏过程中，中途任意一支队伍玩家数量为 0 ，比赛终止
        if (this.isGaming() && (getHostParty() == null || getHostParty().getOnlinePlayers().isEmpty() || getGuestParty() == null || getGuestParty().getOnlinePlayers().isEmpty())) {
            this.setFinishReason(GameFinishReason.MISSING_COMPANION);
        }
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
            if (party.contains(damager) && !isFriendlyFire()) {
                // 同在一个队伍中, 且关闭了友方误伤
                return false;
            }
        }
        return super.willPlayerTakesDamage(player, damager);
    }

    @Override
    protected void onGameTimerUpdate(GameTimer timer) {
        if (timer.getName().equals(Game.WAIT_PERIOD_TIMER_NAME)) {
            if (getGameParties().size() == 1) {
                getStatusBar().setTitle(getGameLocaleString("status-title-waiting-party", false));
                getStatusBar().setColor(BarColor.RED);
            } else if (getGameParties().size() == 2) {
                getStatusBar().setTitle(getGameLocaleString("status-title-waiting-ready", false));
                getStatusBar().setColor(BarColor.YELLOW);
            }
        } else if (timer.getName().equals(Game.GAME_PERIOD_TIMER_NAME)) {
            if (timer.getPeriod() - timer.getCurrent() < 5) {
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
        } else if (timer.getName().equals(Game.FINISH_PERIOD_TIMER_NAME)) {
            getStatusBar().setTitle(getGameLocaleString("status-title-game-finsih", false));
            getStatusBar().setColor(BarColor.BLUE);
        }
    }

    /**
     * 给玩家一个短时间的无敌与发光效果
     *
     * @param player 玩家
     * @param period 持续时间
     */
    private void givePlayerInvincibleBuffEffect(Player player, int period) {
        /**
         * 需要测试，退出，销毁之后，是否还具有这种效果
         */
        new GameBuffEffect("respawn-invincible", player, period, this) {
            @Override
            protected void onFinish() {
                // onFinish 似乎不是每次都执行
                getGame().setPlayerInvincible(getPlayer(), false);
                getPlayer().setGlowing(false);
                log(String.format("%s 被解除了 无敌 效果", player.getName(), getPeriod()));
                super.onFinish();
            }

            @Override
            protected void onStart() {
                getGame().setPlayerInvincible(getPlayer(), true);
                getPlayer().setGlowing(true);
                log(String.format("%s 被赋予了 %s 秒 无敌 效果", player.getName(), getPeriod()));
                super.onStart();
            }

            @Override
            protected void onUpdate() {
                super.onUpdate();
            }
        }.start();
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (getHostParty().contains(player)) {
            event.setRespawnLocation(getGameMap().getSpawn().get("host"));
            givePlayerInvincibleBuffEffect(player, 5);
        } else if (getGuestParty().contains(player)) {
            event.setRespawnLocation(getGameMap().getSpawn().get("guest"));
            givePlayerInvincibleBuffEffect(player, 5);
        } else event.setRespawnLocation(getGameMap().getSpawn().get("default"));
        super.onPlayerRespawn(event);
    }

    @Override
    public void onPlayerDamageByPlayer(Player player, Player damager, EntityDamageByEntityEvent eventSource) {
        if (pvpStatstic != null && isGaming()) {
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
        Player player = event.getPlayer();
        Party party = getManager().getPlayerParty(player);
        // 但是玩家的队伍已经不存在了，结束游戏
        if (party == null) {
            leaveGame(player);
            player.teleport(getGameMap().getLobby());
            player.setBedSpawnLocation(getGameMap().getLobby(), true);
            player.setGameMode(GameMode.SURVIVAL);
        }
        // 玩家在队伍还在，游戏还在继续
        else {
            if (isGaming()) {
                if (party.equals(getHostParty())) {
                    player.teleport(getGameMap().getSpawn().get("host"));
                } else if (party.equals(getGuestParty())) {
                    player.teleport(getGameMap().getSpawn().get("guest"));
                }
            } else {
                player.teleport(getGameMap().getSpawn().get("default"));
            }
        }
        super.onPlayerJoin(event);
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (pvpItemController != null) {
            pvpItemController.recoverItemForPlayer(event.getPlayer());
        }
        super.onPlayerQuit(event);
    }

}
