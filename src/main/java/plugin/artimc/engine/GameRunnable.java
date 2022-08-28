package plugin.artimc.engine;

import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import plugin.artimc.*;
import plugin.artimc.engine.event.PlayerJoinGameEvent;
import plugin.artimc.engine.event.PlayerLeaveGameEvent;
import plugin.artimc.engine.timer.*;
import plugin.artimc.engine.timer.custom.CustomTimer;
import plugin.artimc.engine.timer.effect.PlayerEffect;
import plugin.artimc.engine.timer.internal.FinishPeriodTimer;
import plugin.artimc.engine.timer.internal.GamePeriodTimer;
import plugin.artimc.engine.timer.internal.WaitPeriodTimer;
import plugin.artimc.engine.world.GameWorld;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * 描述：GameRunnable，游戏主流程框架
 * 处理游戏流程：等待、开始、结束、关闭
 * 处理游戏时间刻、计时器运行、游戏时间刻与计时器事件分发
 * 作者：Leo
 * 创建时间：2022/8/25 15:46
 */
public abstract class GameRunnable extends BukkitRunnable implements IGame {
    private final ArtimcPlugin plugin;
    private final TimerManager timerManager;
    protected GameMap gameMap;
    private final StatusBar statusBar;
    private final String gameName;
    private GameWorld gameWorld;
    private int currentTick = 0;
    private boolean running = false;
    private GameStatus gameStatus;
    private FinishReason finishReason = null;
    private CloseReason closeReason = null;
    protected Set<UUID> players;
    protected NameTagManager nameTagManager;
    private final Set<Mechanism> mechanisms;

    /**
     * 游戏初始化
     * 时间刻开始流动，此时游戏并未开始
     * 使用时间管理器，开启游戏主流程
     *
     * @param plugin
     */
    protected GameRunnable(String gameName, ArtimcPlugin plugin) {
        this.gameName = gameName;
        this.plugin = plugin;
        this.gameMap = new GameMap(gameName, plugin);
        this.gameStatus = GameStatus.INITIALIZED;
        this.statusBar = new StatusBar(this);
        this.timerManager = new TimerManager(this);
        this.players = new HashSet<>();
        this.nameTagManager = new NameTagManager(this);
        this.mechanisms = new HashSet<>();
        onInitialization();
        getGameManager().add(gameName, this);
        initializeGameWorld();
        initializeStartupTimer();
        runGame();
    }

    @Override
    public void setNameTagManager(NameTagManager nameTagManager) {
        this.nameTagManager = nameTagManager;
    }

    @Override
    public void useMechanisms(Mechanism... mechanisms) {
        this.mechanisms.addAll(Set.of(mechanisms));
    }

    @Override
    public Set<Mechanism> getMechanisms() {
        return mechanisms;
    }

    private void dispatchMechanisms(Consumer<Mechanism> action) {
        mechanisms.forEach(action);
    }

    private void initializeGameWorld() {
        gameWorld = getWorldManager().get(getWorldName());
        if (gameWorld == null) {
            gameWorld = new GameWorld(getWorldName(), getMap().getEnvironment(), getPlugin());
            getWorldManager().add(getWorldName(), gameWorld);
        }
        // 隐藏成就
        gameWorld.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        // 死亡不掉落
        gameWorld.getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
        // 关闭袭击
        gameWorld.getWorld().setGameRule(GameRule.DISABLE_RAIDS, true);
        // 关闭日月交替
        gameWorld.getWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        // 关闭生物生成
        gameWorld.getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
        // 关闭天气
        gameWorld.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        // 观察者不生成区块
        gameWorld.getWorld().setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        // 永远白天
        gameWorld.getWorld().setTime(6000);
    }

    /**
     * 注册游戏机制
     *
     * @param mechanism
     */
    protected void addMechanism(Mechanism mechanism) {
        mechanisms.add(mechanism);
    }

    protected abstract void onInitialization();

    public void log(String message) {
        boolean debug = getPlugin().getConfig().getBoolean("debug", false);
        if (!debug) return;
        String prefix = String.format("[%s]: ", getGameName());
        getPlugin().getLogger().info(prefix + message);
    }

    @Deprecated
    public String getGameName() {
        return gameName;
    }

    public String getName() {
        return gameName;
    }

    @Deprecated
    public GameMap getGameMap() {
        return gameMap;
    }

    public GameMap getMap() {
        return gameMap;
    }

    @Override
    public String getWorldName() {
        return gameMap.getWorldName();
    }

    public GameWorld getGameWorld() {
        return gameWorld;
    }

    @Deprecated
    protected void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    protected void SetMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    /**
     * 获取当前游戏中的玩家
     *
     * @return
     */
    @Override
    public Set<UUID> getPlayers() {
        return players;
    }

    /**
     * 初始化初始计时器
     * 包含等待、游戏进行、游戏结束三个计时器
     * 游戏中的三个核心计时器，控制着游戏的主流程
     */
    private void initializeStartupTimer() {
        timerManager.addTimer(new WaitPeriodTimer(getGamingPeriod(), statusBar));
        timerManager.addTimer(new GamePeriodTimer(getGamingPeriod(), statusBar));
        timerManager.addTimer(new FinishPeriodTimer(getFinishPeriod(), statusBar));
    }

    /**
     * 获取当前Bukkit服务器
     *
     * @return
     */
    public @NotNull Server getServer() {
        return plugin.getServer();
    }

    public @NotNull Logger getLogger() {
        return getServer().getLogger();
    }

    @Override
    public WorldManager getWorldManager() {
        return getManager().getWorldManager();
    }

    @Override
    public PlayerGameManager getPlayerGameManager() {
        return getManager().getPlayerGameManager();
    }

    @Override
    public PlayerPartyManager getPlayerPartyManager() {
        return getManager().getPlayerPartyManager();
    }

    @Override
    public PlayerChannelManager getPlayerChannelManager() {
        return getManager().getPlayerChannelManager();
    }

    @Override
    public GameManager getGameManager() {
        return getManager().getGameManager();
    }

    /**
     * 获取游戏的剩余时间
     *
     * @return
     */
    @Override
    public int getGameLeftTime() {
        if (timerManager.isTimerRunning(TimerManager.GAME_PERIOD_TIMER)) {
            return timerManager.get(TimerManager.GAME_PERIOD_TIMER).getCurrent();
        }
        return -1;
    }

    /**
     * 获取游戏过去了多少秒
     *
     * @return
     */
    @Override
    public int getGamePassedTime() {
        if (timerManager.isTimerRunning(TimerManager.GAME_PERIOD_TIMER)) {
            return timerManager.get(TimerManager.GAME_PERIOD_TIMER).getPassedTime();
        }
        return -1;
    }

    /**
     * 开始运行游戏
     * 时间刻开始流动
     */
    protected void runGame() {
        // 标记正在运行
        running = true;
        // 时间刻开始运行
        runTaskTimer(plugin, 0, 0);
        // 当前状态等待中
        gameStatus = GameStatus.WAITING;
        // 开启等待玩家加入计时器
        timerManager.waitingCompanions();
        // 执行游戏开始运行事件
        onGameRun();
    }

    /**
     * 当游戏运行的时候触发
     */
    protected void onGameRun() {
    }

    /**
     * 游戏是否正在运行
     *
     * @return
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 游戏的时间管理器
     *
     * @return
     */
    public TimerManager getTimerManager() {
        return timerManager;
    }

    /**
     * 获取 artimc plugin
     *
     * @return
     */
    public ArtimcPlugin getPlugin() {
        return plugin;
    }

    /**
     * 游戏管理器
     *
     * @return
     */
    public ArtimcManager getManager() {
        return plugin.getManager();
    }

    /**
     * 游戏是否处于等待中
     *
     * @return
     */
    public boolean isWaiting() {
        return gameStatus.equals(GameStatus.WAITING);
    }

    /**
     * 游戏是否已经开始
     *
     * @return
     */
    public boolean isGaming() {
        return gameStatus.equals(GameStatus.GAMING);
    }

    /**
     * 游戏是否已经结束
     *
     * @return
     */
    public boolean isFinish() {
        return gameStatus.equals(GameStatus.FINISH);
    }

    /**
     * 游戏是否正在关闭
     *
     * @return
     */
    public boolean isClosing() {
        return gameStatus.equals(GameStatus.CLOSING);
    }

    /**
     * 获取当前的时间刻
     *
     * @return
     */
    public int getCurrentTick() {
        return currentTick;
    }

    /**
     * 获取当前游戏状态
     *
     * @return
     */
    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public FinishReason getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(FinishReason finishReason) {
        this.finishReason = finishReason;
    }

    public CloseReason getCloseReason() {
        return closeReason;
    }

    public void setCloseReason(CloseReason closeReason) {
        this.closeReason = closeReason;
    }

    /**
     * 表示该局游戏的等待时长
     * -1：表示立即开始
     *
     * @return
     */
    public abstract int getWaitingPeriod();

    /**
     * 表示该局游戏的游戏时长
     * -1 表示无限时间
     *
     * @return
     */
    public abstract int getGamingPeriod();

    /**
     * 表示该局游戏结束时长
     * -1 表示立即结束
     *
     * @return
     */
    public abstract int getFinishPeriod();

    protected abstract void onUpdate();

    protected abstract void onFixedUpdate();

    /**
     * 游戏等待期间计时器运行
     *
     * @param timer
     */
    public void onWaitPeriodUpdate(GameTimer timer) {
        dispatchMechanisms(p -> p.onWaitPeriodUpdate(timer));
    }

    /**
     * 游戏过程中计时器运行
     *
     * @param timer
     */
    public void onGamePeriodUpdate(GameTimer timer) {
        dispatchMechanisms(p -> p.onGamePeriodUpdate(timer));
    }

    /**
     * 游戏结束之后计时器运行
     *
     * @param timer
     */
    public void onFinishPeriodUpdate(GameTimer timer) {
        dispatchMechanisms(p -> p.onFinishPeriodUpdate(timer));
    }

    /**
     * 其他游戏计时器运行
     *
     * @param timer
     */
    public void onGameTimerUpdate(GameTimer timer) {
        dispatchMechanisms(p -> p.onGameTimerUpdate(timer));
    }

    /**
     * 游戏开始条件
     *
     * @return
     */
    protected boolean willGameStart() {
        return false;
    }

    /**
     * 游戏结束条件
     *
     * @return
     */
    protected FinishReason willGameFinish() {
        return this.finishReason;
    }

    /**
     * 游戏关闭条件
     *
     * @return
     */
    protected CloseReason willGameClose() {
        return this.closeReason;
    }

    /**
     * 游戏开始事件
     */
    protected void onGameStart() {

    }

    /**
     * 游戏结束事件
     *
     * @param finishReason
     */
    protected void onGameFinish(FinishReason finishReason) {

    }

    /**
     * 游戏关闭事件
     *
     * @param closeReason
     */
    protected void onGameClose(CloseReason closeReason) {

    }

    private void gameStart() {
        gameStatus = GameStatus.GAMING;
        try {
            // 时间管理器执行开始游戏
            timerManager.startingGame();
            // 执行游戏开始事件
            onGameStart();
            log(String.format("游戏开始 tick: %s", getCurrentTick()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gameFinish(FinishReason finishReason) {
        gameStatus = GameStatus.FINISH;
        try {
            // 执行游戏结束事件
            onGameFinish(finishReason);
            log(String.format("游戏结束，原因: %s, tick: %s", finishReason, getCurrentTick()));
            // 时间管理器执行游戏结束
            timerManager.finishingGame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void gameClose(CloseReason closeReason) {
        gameStatus = GameStatus.CLOSING;
        try {
            onGameClose(closeReason);
            log(String.format("游戏关闭，原因: %s, tick: %s", closeReason, getCurrentTick()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            // 停止所有游戏机制的监听
            mechanisms.clear();
            // 复原所有的玩家名称
            nameTagManager.clear();
            // 清除计时器
            timerManager.clear();
            // 玩家清除状态条
            statusBar.getBossBar().removeAll();
            // 游戏结束标记t
            running = false;
            // 停止时间刻流动
            cancel();
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cycleOfGameStart() {
        if (willGameStart() && !isGaming()) {
            gameStart();
        }
    }

    private void cycleOfGameFinish() {
        FinishReason reason = willGameFinish();
        if (reason != null && !isFinish()) {
            gameFinish(reason);
        }
    }

    private void cycleOfGameClose() {
        CloseReason reason = willGameClose();
        if (reason != null && !isClosing()) {
            gameClose(reason);
        }
    }

    protected void cycles() {
        cycleOfGameStart();
        cycleOfGameFinish();
        cycleOfGameClose();
    }

    private void runCycles() {
        try {
            cycles();
        } catch (Exception e) {
            plugin.getLogger().warning("Cycles run ERR: " + e.getMessage());
        }
    }

    private void runUpdate() {
        try {
            onUpdate();
        } catch (Exception e) {
            plugin.getLogger().warning("Update run ERR: " + e.getMessage());
        }
    }

    private void runFixedUpdate() {
        try {
            onFixedUpdate();
        } catch (Exception e) {
            plugin.getLogger().warning("FixedUpdate run ERR: " + e.getMessage());
        }
    }

    /**
     * 游戏计时器启动运行
     */
    private void runTimersTick() {
        for (GameTimer timer : timerManager.getTimers()) {
            if (timer.isRunning()) {
                timer.tick();
            }
        }
    }

    @Override
    public void run() {
        try {
            runCycles();
            runUpdate();
            if (currentTick % 20 == 0) {
                runFixedUpdate();
                runTimersTick();
            }

            if (currentTick % 100 == 0) {
                // run debug
                getPlugin().getLogger().info(String.format("[%s] tick: %s. ", getGameName(), String.valueOf(getCurrentTick())));
            }

        } catch (Exception e) {
            plugin.getLogger().warning("RunnableEngine run ERR: " + e.getMessage());
        } finally {
            currentTick++;
        }
    }

    @Override
    public void close() throws Exception {
        getGameManager().remove(getName());
    }

    protected Player getPlayer(UUID player) {
        return getServer().getPlayer(player);
    }

    /**
     * 将玩家加入当前游戏
     *
     * @param player
     * @return
     * @throws IllegalStateException
     */
    protected boolean add(Player player) {
        if (getManager().playerInGame(player)) throw new IllegalStateException("该玩家正在游戏中，无法加入");

        if (players.add(player.getUniqueId())) {
            getPlayerGameManager().add(player.getUniqueId(), this);
            statusBar.getBossBar().addPlayer(player);
            return true;
        }
        return false;
    }

    protected boolean remove(UUID player) {
        if (!getPlayerGameManager().remove(player)) log("玩家已经离开了游戏，但是在Manager中离开失败");

        if (players.remove(player)) {
            if (getPlayer(player) != null) statusBar.getBossBar().removePlayer(getPlayer(player));
            return true;
        }
        return false;
    }

    public boolean contains(UUID player) {
        return players.contains(player);
    }

    public int size() {
        return players.size();
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

    /**
     * 向游戏内所有队伍发送消息
     *
     * @param message
     */
    @Override
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
     * 玩家加入游戏
     *
     * @param event
     */
    public void onPlayerJoinGame(PlayerJoinGameEvent event) {
        add(event.getPlayer());
    }

    /**
     * 玩家离开游戏
     *
     * @param event
     */
    public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
        remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void givePlayerEffect(String name, int period, Player player) {
        timerManager.startTimer(new PlayerEffect(name, player, period, this));
    }

    @Override
    public void startTimer(CustomTimer timer) {
        timer.setGame(this);
        timerManager.startTimer(timer);
    }

    @Override
    public void stopTimer(CustomTimer timer) {

    }

    @Override
    public boolean playerHasEffect(String name, Player player) {
        return timerManager.getTimers().stream().filter(p -> p instanceof PlayerEffect).map(p -> (PlayerEffect) p).filter(p -> p.getPlayer().equals(player) && p.getEffectName().equals(name)).count() > 0;
    }

    @Override
    public void removePlayerEffect(String name, Player player) {
        List<String> effects = timerManager.getTimers().stream().filter(p -> p instanceof PlayerEffect).map(p -> (PlayerEffect) p).filter(p -> p.getPlayer().equals(player) && p.getEffectName().equals(name)).map(p -> p.getName()).toList();
        effects.forEach(p -> timerManager.stopTimer(p));
    }

    @Override
    public void givePlayerEffect(PlayerEffect effect) {
        timerManager.startTimer(effect);
    }


    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        dispatchMechanisms(p -> p.onPlayerJoin(event));
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        dispatchMechanisms(p -> p.onPlayerQuit(event));
        // 复原命名牌
        nameTagManager.reset(event.getPlayer());
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        dispatchMechanisms(p -> p.onPlayerMove(event));
    }

    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        dispatchMechanisms(p -> p.onPlayerDropItem(event));
    }

    @Override
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        dispatchMechanisms(p -> p.onPlayerGameModeChange(event));
    }

    @Override
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        dispatchMechanisms(p -> p.onPlayerToggleFlight(event));
    }

    @Override
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent event) {
        dispatchMechanisms(p -> p.onPlayerFoodLevelChange(event));
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        dispatchMechanisms(p -> p.onPlayerInteract(event));
    }

    @Override
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        dispatchMechanisms(p -> p.onPlayerDamageByEntity(event));
    }

    @Override
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        dispatchMechanisms(p -> p.onPlayerInteractEntity(event));
    }

    @Override
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        dispatchMechanisms(p -> p.onPlayerArmorStandManipulate(event));
    }

    @Override
    public void onEntityShootBow(EntityShootBowEvent event) {
        dispatchMechanisms(p -> p.onEntityShootBow(event));
    }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {
        dispatchMechanisms(p -> p.onProjectileHit(event));
    }

    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        dispatchMechanisms(p -> p.onEntityDamage(event));
    }

    @Override
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        dispatchMechanisms(p -> p.onEntityDamageByEntity(event));
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        dispatchMechanisms(p -> p.onBlockPlace(event));
    }

    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        dispatchMechanisms(p -> p.onPlayerDamage(event));
    }

    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        dispatchMechanisms(p -> p.onPlayerDeath(event));
    }

    @Override
    public void onPlayerShootBow(EntityShootBowEvent event) {
        dispatchMechanisms(p -> p.onPlayerShootBow(event));
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        dispatchMechanisms(p -> p.onBlockBreak(event));
    }

    @Override
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        dispatchMechanisms(p -> p.onEntityChangeBlock(event));
    }

    @Override
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        dispatchMechanisms(p -> p.onPlayerItemConsume(event));
    }

    @Override
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        dispatchMechanisms(p -> p.onPlayerBucketEmpty(event));
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        dispatchMechanisms(p -> p.onPlayerRespawn(event));
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        dispatchMechanisms(p -> p.onPlayerChat(event));
    }

    @Override
    public void onPlayCommandSend(PlayerCommandPreprocessEvent event) {
        dispatchMechanisms(p -> p.onPlayCommandSend(event));
    }
}
