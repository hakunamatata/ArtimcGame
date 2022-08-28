package plugin.artimc.engine;

import org.bukkit.entity.Player;
import plugin.artimc.ArtimcManager;
import plugin.artimc.ArtimcPlugin;
import plugin.artimc.engine.event.PartyJoinGameEvent;
import plugin.artimc.engine.event.PartyLeaveGameEvent;
import plugin.artimc.engine.event.PlayerJoinGameEvent;
import plugin.artimc.engine.event.PlayerLeaveGameEvent;

import java.util.*;
import java.util.stream.Collectors;

public class Companion {
    private static final Set<PartyName> partyNames = Set.of(PartyName.RED, PartyName.ORANGE, PartyName.YELLOW, PartyName.GREEN, PartyName.LIME, PartyName.BLUE, PartyName.PURPLE);

    final String ERR_PARTY_OVERLOAD() {
        return getGameLocaleString("game-parties-overload").replace("%limit%", String.valueOf(getMaxParties()));
    }

    final String ERR_MEMBER_OVERLOAD() {
        return getGameLocaleString("game-members-overload").replace("%limit%", String.valueOf(getMaxMembers()));
    }

    final String ERR_ALREADY_IN_GAME() {
        return getGameLocaleString("ur-party-already-in-game");
    }

    final String ERR_ONLY_PARTY_OWNER_CAN_JOIN_GAME() {
        return getGameLocaleString("not-party-onwer-join-game");
    }

    private Game game;
    private final Party observeParty;
    private final Map<PartyName, Party> parties;
    private final Set<UUID> invinciblePlayers;

    public Companion(Game game) {
        this.game = game;
        this.observeParty = new Party(game);
        this.parties = new HashMap<>();
        this.invinciblePlayers = new HashSet<>();
    }

    public Game getGame() {
        return this.game;
    }

    public ArtimcPlugin getPlugin() {
        return game.getPlugin();
    }

    public ArtimcManager getManager() {
        return game.getManager();
    }

    protected Set<UUID> getPlayers() {
        return game.getPlayers();
    }

    public Party getObserveParty() {
        return observeParty;
    }

    public Map<PartyName, Party> getGameParties() {
        return parties;
    }

    public Set<Party> getParties() {
        return parties.values().stream().collect(Collectors.toSet());
    }

    protected GameMap getGameMap() {
        return game.getGameMap();
    }

    public int getMaxParties() {
        return getGameMap().getMaxParties();
    }

    public int getMaxMembers() {
        return getGameMap().getMaxMembers();
    }

    public void setInvincible(Player player, boolean invincible) {
        if (invincible && !invinciblePlayers.contains(player.getUniqueId()))
            invinciblePlayers.add(player.getUniqueId());
        else if (!invincible && invinciblePlayers.contains(player.getUniqueId()))
            invinciblePlayers.remove(player.getUniqueId());
    }

    public boolean isInvincible(Player player) {
        return invinciblePlayers.contains(player.getUniqueId());
    }

    private String getGameLocaleString(String path) {
        return game.getGameLocaleString(path);
    }

    private String getGameLocaleString(String path, boolean prefix) {
        return game.getGameLocaleString(path, prefix);
    }

    public Set<Player> getOnlinePlayers() {
        Set<Player> players = new HashSet<>();
        for (Party party : parties.values()) {
            players.addAll(party.getOnlinePlayers());
        }
        return players;
    }


    /**
     * 当前游戏中是否已经存在这支队伍
     *
     * @param party
     * @return
     */
    public boolean contains(Party party) {
        return party.getPartyName() != null && parties.containsKey(party.getPartyName());
    }

    /**
     * 获取下一个可用的队伍颜色
     *
     * @return
     */
    private PartyName nextPartyName() {
        if (parties.size() < getMaxParties()) {
            for (PartyName name : partyNames) {
                if (parties.get(name) == null) return name;
            }
        }
        throw new IllegalStateException(ERR_PARTY_OVERLOAD());
    }

    /**
     * 优先填满游戏中的队伍
     * 其次平均队伍中的玩家
     * 分配策略平均分配
     * 此方法不对队伍做任何修改
     *
     * @return 返回游戏队伍，如果没有合适的游戏队伍，返回观察者队伍
     */
    private Party getUnfullParty() {
        // 如果队伍没有满，返回新的队伍
        if (parties.size() < getMaxParties()) return new Party(game);
        // 寻找队员最少的队伍
        Optional<Party> ret = parties.values().stream().sorted(Comparator.comparingInt(Party::size)).findFirst();
        // 有队伍成员未满了，返回队伍
        if (!ret.isEmpty() && ret.get().size() < getMaxMembers()) {
            return ret.get();
        }
        // 均满了，返回观察者
        return observeParty;
    }

    /**
     * 添加玩家
     *
     * @param player
     */
    public void addCompanion(Player player) {
        Party playerParty = getManager().getPlayerParty(player);
        // 玩家没有队伍，获取未满的队伍
        if (playerParty == null) {
            playerParty = getUnfullParty();
            // 表示玩家加入了观察者
            if (observeParty.equals(playerParty)) {
                observerJoin(player);
            }
            // 表示玩家加入了队伍
            else {
                playerJoin(player, playerParty);
            }
        }
        // 如果玩家有队伍
        // 检查队伍的入场条件
        else {
            // 队伍已经满了
            if (parties.size() >= getMaxParties()) throw new IllegalStateException(ERR_PARTY_OVERLOAD());
            // 玩家队伍成员超标
            if (playerParty.size() > getMaxMembers()) throw new IllegalStateException(ERR_MEMBER_OVERLOAD());
            // 队伍已经在游戏中
            if (contains(playerParty)) throw new IllegalStateException(ERR_ALREADY_IN_GAME());
            // 只有队长才能带领队伍进入游戏
            if (!playerParty.isOwner(player)) throw new IllegalStateException(ERR_ONLY_PARTY_OWNER_CAN_JOIN_GAME());
            // 整支队伍加入游戏
            partyJoin(playerParty);
        }
    }

    /**
     * 从游戏中移除一个玩家
     *
     * @param player
     */
    public void removeCompanion(UUID player) {
        Party party = getManager().getPlayerParty(player);
        // 如果玩家当前在一个队伍中
        if (party != null && party.contains(player)) {
            party.leave(player);
            if (party.getOnlinePlayers().isEmpty()) {
                game.onPartyLeaveGame(new PartyLeaveGameEvent(game, party));
                parties.remove(party.getPartyName());
                party.setGame(null);
                party.setPartyName(null);
                party.dismiss();
            }
        }
        game.onPlayerLeaveGame(new PlayerLeaveGameEvent(game, player));
    }

    /**
     * 从游戏中移除一个玩家
     *
     * @param player
     */
    public void removeCompanion(Player player) {
        removeCompanion(player.getUniqueId());
    }


    /**
     * 将玩家移到新的队伍
     *
     * @param player
     * @param newParty
     */
    public void movePlayer(Player player, Party newParty) {
        Party party = getManager().getPlayerParty(player);
        // 目标队伍不存在，或者玩家本来就在目标队伍中，不处理
        if (newParty == null || newParty.contains(player)) return;
        // 玩家没有队伍，或者玩家的队伍并不包含这个玩家（可能被踢了，或者解散了），不处理
        if (party == null || !party.contains(player)) return;
        // 如果玩家是一个队伍的队长，不处理
        if (party.isOwner(player)) return;
        // 将玩家从原来队伍中静默删除
        party.removeSilent(player);
        // 将玩家静默添加到新的队伍
        newParty.addSilent(player);
        // 静默设置玩家当前队伍
        getManager().getPlayerPartyManager().set(player.getUniqueId(), newParty);
        // 如果将玩家移动到观察者
        if (observeParty.equals(newParty)) {
            game.setPlayerAsObserver(player);
        }
        newParty.updateScoreboard();
        party.updateScoreboard();
        player.sendMessage(getGameLocaleString("move-success").replace("%party_name%", party.getName()));
    }

    /**
     * 两个玩家交换队伍
     *
     * @param player
     * @param targetPlayer
     */
    public void switchPlayer(Player player, Player targetPlayer) {
        Party playerParty = getManager().getPlayerParty(player);
        Party targetParty = getManager().getPlayerParty(targetPlayer);
        if (playerParty == null || targetParty == null) return;
        if (playerParty.equals(targetParty)) return;
        if (playerParty.isOwner(player) || targetParty.isOwner(targetPlayer)) return;
        if (!playerParty.contains(player) || !targetParty.contains(targetPlayer)) return;
        movePlayer(player, targetParty);
        movePlayer(targetPlayer, playerParty);
    }

    /**
     * 添加一支新的队伍
     *
     * @param party
     */
    private void addNewParty(Party party) {
        PartyName name = nextPartyName();
        party.setGame(game);
        party.setPartyName(name);
        parties.put(name, party);
        game.onPartyJoinGame(new PartyJoinGameEvent(game, party));
    }

    /**
     * 表示作为一个玩家加入了游戏
     *
     * @param player 当前玩家
     * @param party  当前加入的队伍
     */
    private void playerJoin(Player player, Party party) {

        // 玩家加入的队伍，在游戏中不存在
        if (!parties.containsKey(party)) {
            party.setOwner(player);
            party.join(player);
            addNewParty(party);
        } else {
            party.join(player);
        }
        party.sendMessage(getGameLocaleString("party-join-player").replace("%player_name%", player.getName()));
        game.onPlayerJoinGame(new PlayerJoinGameEvent(game, player, false));
    }

    /**
     * 玩家作为观察者加入游戏
     *
     * @param player 当前玩家
     */
    private void observerJoin(Player player) {
        observeParty.join(player);
        game.onPlayerJoinGame(new PlayerJoinGameEvent(game, player, true));
    }

    /**
     * 作为队伍加入游戏
     * 带领你队伍中的所有成员
     * 一同加入
     *
     * @param party 当前玩家的队伍
     */
    private void partyJoin(Party party) {
        party.trim();
        addNewParty(party);
        for (Player p : party.getOnlinePlayers()) {
            game.onPlayerJoinGame(new PlayerJoinGameEvent(game, p, false));
        }
        party.sendMessage(getGameLocaleString("party-join-game"));
    }

    public void clear() {
        // 所有玩家离开游戏
        UUID[] playersCopy = getPlayers().toArray(new UUID[0]);
        for (UUID player : playersCopy)
            removeCompanion(player);

        // 清除数据
        observeParty.clear();
        parties.clear();
        invinciblePlayers.clear();
    }
}
