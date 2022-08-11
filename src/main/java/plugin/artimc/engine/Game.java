package plugin.artimc.engine;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import plugin.artimc.ArtimcGameManager;
import plugin.artimc.ArtimcGamePlugin;
import plugin.artimc.scoreboard.BaseScoreboard;
import plugin.artimc.scoreboard.GameScoreboard;
import plugin.artimc.scoreboard.PartyScoreboard;

/**
 * 描述：BaseGame，游戏实体
 * 处理游戏中的玩家、计分板、游戏事件处理
 * 处理游戏的底层运行，时间刻等
 * 作者：Leo
 * 创建时间：2022/7/29 20:40
 */
public abstract class Game extends BukkitRunnable implements AutoCloseable {
    public static final String WAIT_PERIOD_TIMER_NAME = "WAITING_PERIOD_TIMER";
    public static final String GAME_PERIOD_TIMER_NAME = "GAMING_PERIOD_TIMER";
    public static final String FINISH_PERIOD_TIMER_NAME = "FINISH_PERIOD_TIMER";
    private static final String defaultTextOfStatusBar = "当前状态";
    private final Plugin plugin;
    private final String gameName;
    private final HashSet<UUID> players;
    private BossBar statusBar;
    private int currentTick = 0;
    private GameStatus gameStatus;
    private final Map<String, GameTimer> timerManager;
    private GameFinishReason finishReason = null;
    private final Party observeParty;
    private final Map<PartyName, Party> gameParties;
    private final Set<Party> winners;
    private final Set<Party> losers;
    private final Set<PartyName> partyNames;
    private boolean friendlyFire = false;
    private boolean isRunning = false;
    private boolean isClosing = false;
    private GameWorld world;
    // 玩家的Buff效果
    private final Set<UUID> invinciblePlayers;


    protected Game(final String gameName, final Plugin plugin) {
        this.gameName = gameName;
        this.plugin = plugin;
        this.players = new HashSet<>();
        this.timerManager = new HashMap<>();
        partyNames = Set.of(PartyName.RED, PartyName.ORANGE, PartyName.YELLOW, PartyName.GREEN, PartyName.LIME, PartyName.BLUE, PartyName.PURPLE);
        gameParties = new EnumMap<>(PartyName.class);
        observeParty = new Party(this, plugin);
        observeParty.setCustomName(getGameLocaleString("observe-party-name"));
        winners = new HashSet<>();
        losers = new HashSet<>();
        invinciblePlayers = new HashSet<>();
        // 组件初始化
        onInitialization();
        world = new GameWorld(this);
        statusBar = Bukkit.createBossBar(defaultTextOfStatusBar, BarColor.RED, BarStyle.SOLID);
        statusBar.setTitle(defaultTextOfStatusBar);
        statusBar.setVisible(true);
        if (!world.exist()) {
            world.reset();
        }
        // 游戏帧开始流动
        startTick();
    }

    protected void log(String message) {
        boolean debug = plugin.getConfig().getBoolean("debug", false);
        if (!debug) return;
        String prefix = String.format("[%s]: ", getGameName());
        getPlugin().getLogger().info(prefix + message);
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public boolean isGaming() {
        return gameStatus == GameStatus.GAMING;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Set<Party> getWinners() {
        return winners;
    }

    public Set<Party> getLosers() {
        return losers;
    }

    protected void setFinishReason(GameFinishReason finishReason) {
        this.finishReason = finishReason;
    }

    protected String getGameLocaleString(final String path) {
        return getLocaleString("game." + path, true);
    }

    protected String getGameLocaleString(final String path, boolean prefix) {
        return getLocaleString("game." + path, prefix);
    }

    protected String getLocaleString(final String path, final boolean prefix) {
        String prefixString = "";
        if (prefix) {
            prefixString = getPlugin().getLocaleString("prefix-game", false).replace("%game%", this.getGameName());
        }
        return prefixString + getPlugin().getLocaleString(path, false);
    }

    public String getGameName() {
        return gameName;
    }

    public ArtimcGamePlugin getPlugin() {
        return (ArtimcGamePlugin) plugin;
    }

    /**
     * 游戏与队伍管理器
     *
     * @return
     */
    protected ArtimcGameManager getManager() {
        return ((ArtimcGamePlugin) plugin).getManager();
    }

    /**
     * 获取游戏地图
     *
     * @return
     */
    public GameMap getGameMap() {
        final ArtimcGamePlugin agp = (ArtimcGamePlugin) plugin;
        return new GameMap(agp.getGameConfigurations().get(gameName), plugin);
    }

    protected Party getObserveParty() {
        return observeParty;
    }

    public Map<PartyName, Party> getGameParties() {
        return gameParties;
    }

    /**
     * 计时器控制器
     *
     * @return
     */
    public Map<String, GameTimer> getTimerManager() {
        return timerManager;
    }

    public void showTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : getOnlinePlayers()) {
            player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
        }
    }

    public void setPlayerInvincible(Player player, boolean invincible) {
        if (invincible && !invinciblePlayers.contains(player.getUniqueId()))
            invinciblePlayers.add(player.getUniqueId());
        else if (!invincible && invinciblePlayers.contains(player.getUniqueId()))
            invinciblePlayers.remove(player.getUniqueId());
    }

    public boolean isPlayerInvincible(Player player) {
        return invinciblePlayers.contains(player.getUniqueId());
    }

    /**
     * 所有组件的初始化
     * 在时间刻开始之前必须完成初始化
     */
    protected abstract void onInitialization();

    /**
     * 玩家加入游戏
     * 自动加入队伍，自动加入观察者，整队自动加入游戏
     */
    public void addCompanion(final Player player) {
        Party gameParty = null;

        // 如果玩家没有队伍，从游戏中寻找未满的队伍
        if (!getManager().playerInParty(player)) {
            gameParty = getUnfullParty();
            playerJoinGame(player, gameParty);
        }
        // 如果玩家有队伍
        else {
            // 队伍满载，抛出异常
            if (gameParties.size() >= getGameMap().getMaxParties())
                throw new IllegalStateException(getGameLocaleString("game-parties-overload").replace("%limit%", String.valueOf(getGameMap().getMaxParties())));

            gameParty = getManager().getPlayerParty(player);

            // 如果玩家队伍人数超过限制，返回异常
            if (gameParty.size() > getGameMap().getMaxMembers()) {
                throw new IllegalStateException(getGameLocaleString("party-members-overload").replace("%limit%", String.valueOf(getGameMap().getMaxMembers())));
            }

            // 如果玩家的队伍 已经在游戏中了，返回异常
            if (gameParty.getPartyName() != null && gameParties.containsKey(gameParty.getPartyName())) {
                throw new IllegalStateException(getGameLocaleString("ur-party-already-in-game"));
            }

            // 只有队长能够带来队伍进入游戏
            if (!gameParty.isOwner(player))
                throw new IllegalStateException(getGameLocaleString("not-party-onwer-join-game"));

            partyJoinGame(gameParty);
        }
    }

    /**
     * 玩家的队伍整队加入游戏
     * 离线的玩家将自动离开队伍
     *
     * @param party 玩家的队伍
     */
    private void partyJoinGame(Party party) {
        // 为新队伍分配颜色
        PartyName partyName = getNextPartyName();
        // 新队伍设置游戏
        party.setGame(this);
        // 新队伍设置颜色
        party.setPartyName(partyName);
        for (Object p : party.getPlayers().toArray().clone()) {
            OfflinePlayer player = getPlugin().getServer().getOfflinePlayer((UUID) p);
            if (player.isOnline()) {
                add((Player) player);
            } else {
                party.leave(player);
            }
        }
        gameParties.put(partyName, party);
        onPartyJoinGame(party);
        // 队伍加入游戏之后，由于是整队加入的游戏
        // 所以需要为队伍内的成员分别执行一次 onPlayerJoinGame 事件
        for (Player player : party.getOnlinePlayers()) {
            onPlayerJoinGame(player, party, false);
        }
        party.sendMessage(getGameLocaleString("party-join-game"));
        log(String.format("%s join the game.", party.getName()));
    }

    /**
     * 当有队伍加入游戏
     *
     * @param party
     */
    protected void onPartyJoinGame(Party party) {
        // 有新的队伍加入游戏，更新玩家的计分板
        changeGameScoreboard(useScoreboard(), this.getClass());
        //useScoreboard();
    }

    /**
     * 当玩家加入游戏时
     *
     * @param player
     * @param party
     */
    protected void onPlayerJoinGame(Player player, Party party, boolean isObserver) {
        player.teleport(getGameMap().getSpawn().get("default"));
        log(String.format("%s join the game.", player.getName()));
    }

    /**
     * 表示玩家加入游戏
     * <p>
     * 1. 加入观察者
     * 2. 加入现有队伍
     * 3. 加入新的队伍
     *
     * @param player 玩家
     * @param party  玩家当前的队伍
     */
    private void playerJoinGame(Player player, Party party) {
        // 如果玩家的队伍时 观察者
        if (observeParty.equals(party)) {
            observeParty.join(player);
            // 游戏中添加玩家
            add(player);
            // 暂不处理
            observeParty.sendMessage(getGameLocaleString("ob-join-player").replace("%player_name%", player.getName()));
            player.teleport(getGameMap().getSpawn().get("default"));
            onPlayerJoinGame(player, party, true);
        }
        // 玩家加入了现有的队伍
        else if (gameParties.values().contains(party)) {
            // 队伍中添加玩家
            party.join(player);
            // 游戏中添加玩家
            add(player);
            party.sendMessage(getGameLocaleString("party-join-player").replace("%player_name%", player.getName()));
            onPlayerJoinGame(player, party, false);
        }
        // 玩家以一个新的队伍加入游戏
        else {
            // 为新队伍分配颜色
            PartyName partyName = getNextPartyName();
            party.join(player);
            party.setOwner(player);
            // 新队伍设置游戏
            party.setGame(this);
            // 新队伍设置颜色
            party.setPartyName(partyName);
            // 游戏中添加
            add(player);
            gameParties.put(partyName, party);
            onPartyJoinGame(party);
            party.sendMessage(getGameLocaleString("party-join-player").replace("%player_name%", player.getName()));
            onPlayerJoinGame(player, party, false);
        }
    }

    /**
     * 获取一个没有满员的队伍
     * 如果所有队伍都满员，返回观察者队伍
     * 优先将队伍填满，其次将队伍成员填满
     * 分配策略平均分配
     * 此方法不对队伍做任何修改
     *
     * @return 返回游戏队伍，如果没有合适的游戏队伍，返回观察者队伍
     */
    private Party getUnfullParty() {
        Party ret = null;
        // 优先填满队伍，队伍中至少 1 个玩家
        int maxParties = getGameMap().getMaxParties();
        // 如果队伍没有满，返回新的队伍
        if (gameParties.size() < maxParties) {
            ret = new Party(this, plugin);
        }
        // 队伍已经填满，寻找没有满员的队伍
        else {
            final int maxMembers = getGameMap().getMaxMembers();
            // 1. 当前游戏中找一个没有满员的队伍
            //    寻找成员最少的队伍加入
            int minPartyMember = 99999;
            for (final Party party : gameParties.values()) {
                // 找到没有满员的队伍了
                if (party.size() < maxMembers && party.size() < minPartyMember) {
                    minPartyMember = party.size();
                    ret = party;
                }
            }
            // 如果全都满员了，返回观察者队伍
            if (ret == null) ret = observeParty;
        }
        return ret;
    }

    /**
     * 获取下一个可用的队伍颜色
     *
     * @return
     */
    private PartyName getNextPartyName() {
        for (final PartyName pn : partyNames) {
            if (gameParties.get(pn) == null) {
                return pn;
            }
        }
        throw new IllegalStateException();
    }

    /**
     * 游戏等待时间超时
     */
    protected void onGameTimeout() {

    }

    /**
     * 计时器走动
     *
     * @param timer
     */
    protected void onGameTimerUpdate(final GameTimer timer) {

    }

    /**
     * 游戏帧开始流动
     */
    private void startTick() {
        isRunning = true;
        gameStatus = GameStatus.WAITING;
        // 时间刻开始运行
        runTaskTimer(plugin, 0, 0);
        gameCreate();
    }

    private void gameCreate() {
        /**
         * 等待队伍加入的计时器
         */
        new GameTimer(WAIT_PERIOD_TIMER_NAME, getGameMap().getWaitPeriod(), this) {
            @Override
            protected void onFinish() {
                log(String.format("计时器： %s 结束", WAIT_PERIOD_TIMER_NAME));
                // 等待时间结束，但是游戏并未开始
                if (!isGaming()) {
                    log(String.format("游戏等待超时，即将关闭游戏"));
                    // 超时
                    onGameTimeout();
                    // 关闭游戏
                    if (!isClosing) closeGame();
                }

                super.onFinish();
            }

            @Override
            protected void onUpdate() {
                getStatusBar().setProgress((double) getCurrent() / (double) getPeriod());
                super.onUpdate();
            }
        }.start();

        log(String.format("计时器： %s 启动", WAIT_PERIOD_TIMER_NAME));
        onGameCreate();
    }

    /**
     * 游戏创建成功
     * 已经完成了初始化、时间刻已经开始启动
     */
    protected void onGameCreate() {

    }

    /**
     * 游戏是否可以开始，满足条件之后游戏自动从
     * 等待中 切换为 游戏开始
     */
    protected boolean willGameStart() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * 游戏结束标志，当返回null时，游戏继续执行
     * 当返回 GameFinishReason 时，游戏结束
     */
    protected GameFinishReason willGameFinish() {
        return finishReason;
    }

    protected Class<? extends GameScoreboard> useScoreboard() {
        return GameScoreboard.class;
    }

    // 修改计分板
    private void changeGameScoreboard(Class<? extends GameScoreboard> type, Class<? extends Game> game) {
        // 将游戏内队伍的计分板设置为游戏计分板
        try {
            Constructor<? extends GameScoreboard> ct = type.getDeclaredConstructor(BaseScoreboard.class, game);
            for (Party party : gameParties.values()) {
                GameScoreboard newScoreboard = ct.newInstance(party.getScoreboard(), this);
                party.setScoreboard(newScoreboard);
            }
        } catch (Exception ex) {
            sendMessage(getGameLocaleString("scoreboard-unsupport: ") + ex.getMessage());
        }
    }

    private void gameStart() {
        if (timerManager.get(Game.WAIT_PERIOD_TIMER_NAME) != null)
            timerManager.get(Game.WAIT_PERIOD_TIMER_NAME).close();
        // 修改队伍的计分板
        changeGameScoreboard(useScoreboard(), this.getClass());
        /**
         * 游戏开始计时器
         */
        new GameTimer(GAME_PERIOD_TIMER_NAME, getGameMap().getGamePeriod(), this) {
            @Override
            protected void onFinish() {
                log(String.format("计时器：%s 结束", GAME_PERIOD_TIMER_NAME));
                if (getGameStatus() != GameStatus.FINISH) {
                    finishReason = GameFinishReason.GAMING_TIMEOUT;
                    log(String.format("游戏即将结束，原因：游戏时间已到"));
                }
            }

            @Override
            protected void onUpdate() {
                getStatusBar().setProgress((double) getCurrent() / (double) getPeriod());
            }
        }.start();
        log(String.format("游戏开始，计时器：%s 启动", GAME_PERIOD_TIMER_NAME));
        onGameStart();

    }

    /**
     * 当游戏从 等待中 切换成 游戏开始 之后执行
     */
    protected void onGameStart() {

    }

    /**
     * 游戏结束，进入结算期
     *
     * @param reason
     */
    private void gameFinish(final GameFinishReason reason) {
        if (timerManager.get(Game.GAME_PERIOD_TIMER_NAME) != null)
            timerManager.get(Game.GAME_PERIOD_TIMER_NAME).close();
        finishReason = reason;
        log(String.format("游戏结束，原因：%s", reason));
        /**
         * 游戏结束计时器
         */
        new GameTimer(FINISH_PERIOD_TIMER_NAME, getGameMap().getFinishPeriod(), this) {

            @Override
            protected void onFinish() {
                log(String.format("计时器：%s 结束", FINISH_PERIOD_TIMER_NAME));
                if (!isClosing) {
                    closeGame();
                    log(String.format("游戏已经关闭"));
                }
                super.onFinish();
            }

            @Override
            protected void onUpdate() {
                getStatusBar().setProgress((double) getCurrent() / (double) getPeriod());
                super.onUpdate();
            }
        }.start();
        /**
         * 设置胜负队伍
         */
        for (Party party : gameParties.values()) {
            if (willWinTheGame(party)) {
                winners.add(party);
            } else {
                losers.add(party);
            }
        }
        log(String.format("游戏结束，计时器：%s 启动", FINISH_PERIOD_TIMER_NAME));
        onGameFinish(reason);
    }


    /**
     * 当游戏结束时触发执行
     */
    protected void onGameFinish(final GameFinishReason reason) {
        if (reason == GameFinishReason.NO_COMPANION) {
            sendMessage("&7没有玩家加入，游戏结束");
        }
    }

    /**
     * 胜利条件
     *
     * @return
     */
    protected abstract boolean willWinTheGame(Party party);

    /**
     * 每时刻都在执行
     */
    protected abstract void onUpdate();

    /**
     * 游戏被关闭之前执行
     */
    protected void onGameClose() {

    }

    /**
     * 获取当前状态进度条
     *
     * @return
     */
    public BossBar getStatusBar() {
        return statusBar;
    }

    /**
     * 获取当前游戏中的玩家
     *
     * @return
     */
    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    /**
     * 获取当前游戏中的在线玩家
     *
     * @return
     */
    public Set<Player> getOnlinePlayers() {
        return getManager().getOnlinePlayers(players);
    }

    /**
     * 将玩家加入当前游戏
     *
     * @param player
     * @return
     * @throws IllegalStateException
     */
    protected boolean add(final UUID player) throws IllegalStateException {
        if (!getManager().joinGame(player, this)) throw new IllegalStateException("该玩家在游玩其他游戏，无法加入");
        return players.add(player);
    }

    /**
     * 将当前玩家加入游戏
     *
     * @param player
     * @return
     */
    protected boolean add(final Player player) {
        statusBar.addPlayer(player);
        return add(player.getUniqueId());
    }

    /**
     * 将玩家从当前游戏中移除
     *
     * @param player
     * @return
     */
    public boolean remove(final UUID player) {
        if (players.remove(player)) {
            if (!getManager().leaveGame(player)) {
                plugin.getLogger().warning("玩家已经离开了游戏，但是在Manager中离开失败");
            }
            return true;
        }
        return false;
    }

    /**
     * 检测玩家是否在当前游戏中
     *
     * @param player
     * @return
     */
    public boolean contains(final UUID player) {
        return players.contains(player);
    }

    /**
     * 检测玩家时是否在当前游戏中
     *
     * @param player
     * @return
     */
    public boolean contains(final Player player) {
        return contains(player.getUniqueId());
    }

    /**
     * 获得当前游戏中的玩家数量
     *
     * @return
     */
    public int size() {
        return players.size();
    }

    /**
     * 设置游戏中玩家是否可以看见其他玩家
     *
     * @param value
     */
    public void setInvisibility(final boolean value) {
        final Set<Player> onlinePlayers = getOnlinePlayers();
        for (final Player player : onlinePlayers) {
            for (final Player p : onlinePlayers) {
                if (p != player) {
                    if (value) player.hidePlayer(plugin, p);
                    else player.showPlayer(plugin, p);
                }
            }
        }
    }

    /**
     * 获取当前游戏状态
     *
     * @return
     */
    public GameStatus getGameStatus() {
        return gameStatus;
    }

    /**
     * 玩家离开游戏事件
     *
     * @param player
     */
    protected void onPlayerLeaveGame(Player player) {

    }

    /**
     * 玩家离开游戏
     *
     * @param player
     */
    public void leaveGame(Player player) {
        if (statusBar != null) // update UI
            statusBar.removePlayer(player);
        // 游戏中将玩家移除
        remove(player.getUniqueId());
        player.teleport(getGameMap().getLobby());
        // 玩家离开队伍，更新玩家计分板
        Party party = getManager().getPlayerParty(player);
        if (party != null) {
            party.leave(player);
            // 更新玩家当前队伍的计分板
            // 如果队伍中成员为空，将该队伍移除游戏
            if (party.getOnlinePlayers().isEmpty()) {
                gameParties.remove(party.getPartyName());
                party.setGame(null);
                party.setPartyName(null);
                party.dismiss();
            }
        }
        log(String.format("玩家 %s 离开了游戏", player.getName()));
        onPlayerLeaveGame(player);
    }


    public void closeGame() {
        if (timerManager.get(Game.FINISH_PERIOD_TIMER_NAME) != null)
            timerManager.get(Game.FINISH_PERIOD_TIMER_NAME).close();

        isClosing = true;
        // 停止游戏
        cancel();
        setInvisibility(false);
        isRunning = false;
        // 清理计时器
        try {
            for (GameTimer timer : timerManager.values()) {
                timer.close();
            }
        } catch (Exception ex) {
            log(ex.getMessage());
        }
        timerManager.clear();
        onGameClosing();
        // 清空数据
        // 将队伍的游戏删掉
        statusBar.removeAll();
        // 将游戏中的队伍清理掉
        for (Party party : gameParties.values()) {
            gameParties.remove(party.getPartyName());
            party.sendMessage(getGameLocaleString("game-is-closed"));
            party.setGame(null);
            party.setPartyName(null);
            // 将游戏内队伍的计分板设置为队伍计分板
            party.setScoreboard(new PartyScoreboard(party.getScoreboard(), party));
            party.updateScoreboard();
            for (UUID player : party.getPlayers()) {
                remove(player);
                OfflinePlayer offline = getPlugin().getServer().getOfflinePlayer(player);
                if (offline.isOnline()) {
                    ((Player) offline).teleport(getGameMap().getLobby());
                }
            }
        }
        // 清理队伍
        gameParties.clear();
        // 清理玩家
        players.clear();
        winners.clear();
        losers.clear();
        invinciblePlayers.clear();
        playerPlacedBlocks.clear();
        // 游戏结束重置地形
        world.reset();
        log(String.format("地形已重置"));
        onGameClose();
        close();
    }

    /**
     * 关闭游戏
     */
    public void close() {
        log(String.format("游戏已释放"));
    }

    /**
     * 游戏即将关闭
     * 此时游戏已经停止，但是玩家和队伍还没有删除
     */
    protected void onGameClosing() {
        for (Party party : gameParties.values()) {
            party.teleport(getGameMap().getLobby());
        }
    }

    /**
     * 获取游戏当前时间刻
     *
     * @return
     */
    public int getCurrentTick() {
        return currentTick;
    }

    /**
     * 每个时间刻都会执行
     */
    @Override
    public void run() {
        try {
            cycleOfGameStart();
            cycleOfGaming();
            onUpdate();
            if (currentTick % 20 == 0) {
                for (final Party party : gameParties.values()) {
                    party.updateScoreboard();
                }
                // 每秒钟运行
                onFixedUpdate();
            }
            // 强制停止游戏
            if (!isClosing && willForceCloseGame()) {
                closeGame();
            }
        }
        // 不能让异常中断游戏
        catch (Exception ex) {
            getPlugin().getServer().getLogger().warning("onTick Exception: " + ex.getMessage());
        }
        // 无论如何，游戏时间刻都在增加
        finally {
            // 时刻都在运行
            currentTick++;
        }

    }

    /**
     * 检测游戏是否可以开始
     */
    private void cycleOfGameStart() {
        if (gameStatus == GameStatus.WAITING && willGameStart()) {
            gameStatus = GameStatus.GAMING;
            gameStart();
        }
    }

    /**
     * 检测游戏是否可以结束
     */
    private void cycleOfGaming() {
        GameFinishReason reason = null;
        if (isGaming()) {
            reason = willGameFinish();
            // 如果 当前游戏还没有结束条件，但是此时有了结束条件，游戏结束
            if (reason != null) {
                gameStatus = GameStatus.FINISH;
                gameFinish(reason);
                sendMessage(getGameLocaleString("game-is-finish", false));
            }
        }
    }

    private void resetPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setGlowing(false);
        player.setHealthScaled(false);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        setPlayerInvincible(player, false);
    }

    /**
     * 需要确保时有玩家在场
     * 当游戏内没有玩家的时候，这个游戏就已经没有存在的必要了
     * 在游戏创建的5秒后，没有玩家在场，就可以释放了
     *
     * @return
     */
    protected boolean willForceCloseGame() {
        return currentTick > 5 * 20 && getOnlinePlayers().isEmpty();
    }

    /**
     * 计时器结束
     *
     * @param timer
     */
    protected void onGameTimerFinish(final GameTimer timer) {

    }

    /**
     * 固定每秒运行
     */
    protected void onFixedUpdate() {

        final Object[] timers = timerManager.values().toArray().clone();
        for (final Object o : timers) {
            final GameTimer timer = (GameTimer) o;
            if (timer.getCurrent() <= 0) {
                timer.stop();
                onGameTimerFinish(timer);
                timerManager.remove(timer.getName());
            } else {
                timer.tick();
                onGameTimerUpdate(timer);
            }
        }
    }

    /**
     * 向游戏内所有队伍发送消息
     *
     * @param message
     */
    public void sendMessage(String message) {
        // prefix-game: "&6[%game%]&f "
        message = getLocaleString("prefix-game", false).replace("%game%", getGameName()) + ChatColor.translateAlternateColorCodes('&', message);
        for (UUID uuid : players) {
            OfflinePlayer player = getPlugin().getServer().getOfflinePlayer(uuid);
            if (player.isOnline()) {
                ((Player) player).sendMessage(message);
            }
        }
    }

    /**
     * This will be called when a player in this game joins the server
     *
     * @param event PlayerJoinEvent
     */
    public void onPlayerJoin(final PlayerJoinEvent event) {

    }

    /**
     * This will be called when a player in this game quits the server
     *
     * @param event PlayerQuitEvent
     */
    public void onPlayerQuit(final PlayerQuitEvent event) {
        // 玩家在游戏过程中退出，设置生成位置为大厅
        event.getPlayer().setBedSpawnLocation(getGameMap().getLobby(), true);
        // 玩家在游戏过程中退出视为离开游戏，且离开队伍
        event.getPlayer().setGlowing(false);
        // 玩家下线 表示离开游戏
        resetPlayer(event.getPlayer());
        leaveGame(event.getPlayer());

    }

    /**
     * This will be called when a player in this game moves
     *
     * @param event PlayerMoveEvent
     */
    public void onPlayerMove(final PlayerMoveEvent event) {

    }

    /**
     * This will be called when a player in this game drops an item
     *
     * @param event PlayerDropItemEvent
     */
    public void onPlayerDropItem(final PlayerDropItemEvent event) {

    }

    /**
     * This will be called when a player in this game changes game mode
     *
     * @param event PlayerGameModeChangeEvent
     */
    public void onPlayerGameModeChange(final PlayerGameModeChangeEvent event) {

    }

    /**
     * This will be called when a player in this game toggles flight
     *
     * @param event PlayerToggleFlightEvent
     */
    public void onPlayerToggleFlight(final PlayerToggleFlightEvent event) {

    }

    /**
     * This will be called when a player in this game's hunger changes
     *
     * @param event FoodLevelChangeEvent
     */
    public void onPlayerFoodLevelChange(final FoodLevelChangeEvent event) {

    }

    /**
     * This will be called when a player in this game interacts with something
     *
     * @param event PlayerInteractEvent
     */
    public void onPlayerInteract(final PlayerInteractEvent event) {

    }

    /**
     * This will be called when a player in this game interacts with an entity
     *
     * @param event PlayerInteractEntityEvent
     */
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {

    }

    /**
     * This will be called when a player in this game manipulates an armor stand
     *
     * @param event PlayerArmorStandManipulateEvent
     */
    public void onPlayerArmorStandManipulate(final PlayerArmorStandManipulateEvent event) {

    }

    /**
     * This will be called when a player in this game shoots a bow
     *
     * @param event EntityShootBowEvent
     */
    public void onPlayerShootBow(final EntityShootBowEvent event) {

    }

    /**
     * This will be called when a projectile shot by a player in this game hits
     * something
     *
     * @param event ProjectileHitEvent
     */
    public void onProjectileHit(final ProjectileHitEvent event) {

    }

    /**
     * This will be called when a player in this game takes damage
     *
     * @param event EntityDamageEvent
     */
    public void onPlayerDamage(final EntityDamageEvent event) {
        // 游戏未开始时，玩家不受任何伤害
        if (!isGaming()) event.setCancelled(true);
            // 游戏开始时
        else {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                // 如果玩家处于无敌状态，不受伤害
                if (isPlayerInvincible(player)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * This will be called when a player in this game takes damage by another player
     */
    public void onPlayerDamageByPlayer(final Player player, final Player damager, final EntityDamageByEntityEvent eventSource) {

    }

    /**
     * This will be called when a player in this game is damaged by an entity
     *
     * @param event EntityDamageByEntityEvent
     */
    public void onPlayerDamageByEntity(final EntityDamageByEntityEvent event) {
        Player player = (Player) event.getEntity();
        Player damager = null;
        // 如果攻击实体为 Player
        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        }
        // 如果攻击实体不是 Player
        else {
            switch (event.getCause()) {
                case MAGIC:
                case PROJECTILE:
                    Projectile proj = (Projectile) event.getDamager();
                    if (proj.getShooter() instanceof Player) {
                        damager = (Player) proj.getShooter();
                    }
                    break;
            }
        }

        if (damager != null) {
            // 如果玩家不承受伤害者的伤害，将伤害取消
            if (!willPlayerTakesDamage(player, damager)) {
                event.setCancelled(true);
            }

            // 即将造成伤害
            if (!event.isCancelled()) {
                onPlayerDamageByPlayer(player, damager, event);
            }
        }

    }

    protected boolean willPlayerTakesDamage(Player player, Player damager) {
        // 玩家不是无敌的，允许对他造成伤害
        return !isPlayerInvincible(player);
    }

    private final Set<Block> playerPlacedBlocks = new HashSet<>();

    /**
     * This will be called when a player places a block
     *
     * @param event BlockPlaceEvent
     */
    public void onBlockPlace(final BlockPlaceEvent event) {
        // 游戏未开始时，玩家无法放置任何方块
        if (!isGaming()) {
            event.setCancelled(true);
        }
        // 游戏开始之后，只有玩家防止的方块可以摧毁
        else {
            playerPlacedBlocks.add(event.getBlock());
        }

    }

    /**
     * This will be called when a player breaks a block
     *
     * @param event BlockBreakEvent
     */
    public void onBlockBreak(final BlockBreakEvent event) {
        // 游戏未开始时，玩家无法破坏任何方块
        if (!isGaming()) {
            event.setCancelled(true);
        }
        // 游戏开始之后，只有玩家防止的方块可以被摧毁
        else {
            if (playerPlacedBlocks.contains(event.getBlock())) {
                playerPlacedBlocks.remove(event.getBlock());
            } else {
                event.setCancelled(true);
            }
        }
    }


    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
        if (!isGaming()) {
            event.setCancelled(true);
        }
    }

    public void onPlayerItemConsume(final PlayerItemConsumeEvent event) {
    }

    /**
     * This will be called when an entity changes a block
     *
     * @param event EntityChangeBlockEvent
     */
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        // 游戏未开始时，玩家无法破坏任何方块
        if (!isGaming()) event.setCancelled(true);
    }

    /**
     * This will be called when an player dead
     *
     * @param event Entity Death Event
     */
    public void onPlayerDeath(final PlayerDeathEvent event) {

    }

    /**
     * 玩家复活时候执行
     *
     * @param event
     */
    public void onPlayerRespawn(final PlayerRespawnEvent event) {

    }
}
