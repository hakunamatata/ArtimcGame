package plugin.artimc.game;

import java.util.*;

import org.bukkit.entity.Player;

import plugin.artimc.engine.Party;
import plugin.artimc.engine.PartyName;

/**
 * 描述：PvPStatstic，伤害统计 （两个队伍）
 * 处理游戏过程中的伤害数据，包括 造成的伤害、防御伤害、击杀数、助攻数、死亡数
 * 作者：Leo
 * 创建时间：2022/7/29 20:40
 */
public class PvPStatstic {

    private PvPGame game;
    // 玩家造成的伤害
    private Map<UUID, Double> damageMap;
    // 玩家吸收的伤害
    private Map<UUID, Double> defenceMap;

    private Queue<PvPDamage> damageQueue;
    private int contPeriod = 6 * 20;
    private Map<UUID, Integer> killMap;
    private Map<UUID, Integer> deathMap;
    private Map<UUID, Integer> assistMap;

    private Map<PartyName, Integer> partyKills;
    private Map<PartyName, Integer> partyAssists;
    private Map<PartyName, Integer> partyDeaths;
    private Map<PartyName, Double> partyDamages;
    private Map<PartyName, Double> partyDefences;

    private Map<PartyName, Integer> partyScorePlus;

    public PvPStatstic(PvPGame game) {
        this.game = game;
        damageMap = new HashMap<>();
        defenceMap = new HashMap<>();
        killMap = new HashMap<>();
        deathMap = new HashMap<>();
        assistMap = new HashMap<>();
        damageQueue = new LinkedList<>();
        initializePartyStat();
    }

    private void initializePartyStat() {
        partyKills = new HashMap<>();
        partyAssists = new HashMap<>();
        partyDeaths = new HashMap<>();
        partyDamages = new HashMap<>();
        partyDefences = new HashMap<>();
        partyScorePlus = new HashMap<>();
//        for (Party party : game.getGameParties().values()) {
//            partyKills.put(party.getPartyName(), 0);
//            partyAssists.put(party.getPartyName(), 0);
//            partyDeaths.put(party.getPartyName(), 0);
//            partyDamages.put(party.getPartyName(), 0.0);
//            partyDefences.put(party.getPartyName(), 0.0);
//        }
    }

    public void addPartyScore(Party party, int score) {
        PartyName pn = party.getPartyName();
        if (partyScorePlus.containsKey(pn)) {
            int n = partyScorePlus.get(pn);
            partyScorePlus.put(pn, n + score);
        } else {
            partyScorePlus.put(pn, score);
        }
    }

    public int getPartyExtraScore(Party party) {
        return partyScorePlus.getOrDefault(party.getPartyName(), 0);
    }


    /**
     * 计算 造成的伤害与防御数值
     *
     * @param attacker
     * @param defencer
     * @param damage
     */
    public void onAttack(int tick, Player attacker, Player defencer, Double damage) {
        if (attacker instanceof Player && defencer instanceof Player) {
            UUID a = attacker.getUniqueId();
            UUID t = defencer.getUniqueId();
            PartyName apn = game.getPlugin().getManager().getPlayerParty(a).getPartyName();
            PartyName tpn = game.getPlugin().getManager().getPlayerParty(t).getPartyName();
            if (!isEnemy(attacker, defencer)) return;

            if (damageMap.containsKey(a)) {
                Double n = damageMap.getOrDefault(a, 0.00);
                damageMap.put(a, n + damage);
            } else {
                damageMap.put(a, damage);
            }

            if (partyDamages.containsKey(apn)) {
                Double n = partyDamages.getOrDefault(apn, 0.00);
                partyDamages.put(apn, n + damage);
            } else {
                partyDamages.put(apn, damage);
            }


            if (defenceMap.containsKey(t)) {
                Double n = defenceMap.getOrDefault(t, 0.00);
                defenceMap.put(t, n + damage);
            } else {
                defenceMap.put(t, damage);
            }

            if (partyDefences.containsKey(tpn)) {
                Double n = partyDefences.getOrDefault(apn, 0.00);
                partyDefences.put(tpn, n + damage);
            } else {
                partyDefences.put(tpn, damage);
            }

            causeDamage(tick, attacker, defencer, damage);
        }
    }

    /**
     * 获取队伍的表现得分
     *
     * @param party
     * @return
     */
    public double getPartyPerformance(Party party) {
        try {
            PartyName pn = party.getPartyName();
            double party_damage = partyDamages.getOrDefault(pn, 0.0);
            double multiplier = partyKills.getOrDefault(pn, 0) - partyDeaths.getOrDefault(pn, 0);
            if (multiplier <= 0) multiplier = 1;
            else if (multiplier == 1) multiplier += 0.5;
            return party_damage / 10 * multiplier + getPartyExtraScore(party);
        } catch (Exception ex) {
            return 0;
        }
    }


    /**
     * 获取玩家最近一次造成的伤害来源
     * 6秒内最近一次的伤害
     *
     * @param currentTick
     * @param player
     * @return
     */
    private Player getLastDamager(int currentTick, Player player) {
        int maxTick = -1;
        UUID attacker = null;
        // 获取最近一次的攻击玩家
        for (PvPDamage dmg : damageQueue) {
            if (dmg.tick > maxTick && player.getUniqueId().equals(dmg.defencer)) {
                maxTick = dmg.tick;
                attacker = dmg.attacker;
            }
        }
        return game.getPlugin().getManager().getOnlinePlayer(attacker);
    }

    public void onDeath(int tick, Player player, Player killer) {
        if (!(killer instanceof Player)) killer = getLastDamager(tick, player);

        if (killer == null) return;

        incK(killer);
        incD(player);
        for (Player a : getAssists(tick, killer, player)) {
            incA(a);
        }
    }

    private boolean isEnemy(Player attacker, Player target) {
        UUID a = attacker.getUniqueId();
        UUID t = target.getUniqueId();
        boolean sameParty = (game.getHostParty().contains(a) && game.getHostParty().contains(t)) || (game.getGuestParty().contains(a) && game.getGuestParty().contains(t));
        return !sameParty;
    }

    private void causeDamage(int tick, Player attacker, Player defencer, Double damage) {
        updateDamagerQueueInTicks(tick);
        damageQueue.offer(new PvPDamage(tick, attacker.getUniqueId(), defencer.getUniqueId(), damage));
    }

    private void updateDamagerQueueInTicks(int tick) {
        if (damageQueue.isEmpty()) return;
        while (damageQueue.peek() != null && damageQueue.peek().tick + contPeriod < tick) {
            damageQueue.poll();
        }
    }

    private Set<Player> getAssists(int tick, Player killer, Player deadPlayer) {
        UUID uk = killer.getUniqueId();
        UUID ud = deadPlayer.getUniqueId();
        Set<UUID> assists = new HashSet<>();
        for (PvPDamage damage : damageQueue) {
            if (damage.defencer.equals(ud) && (damage.tick > tick - contPeriod) && !assists.contains(damage.attacker) && !damage.attacker.equals(uk)) {
                assists.add(damage.attacker);
            }
        }
        return game.getPlugin().getManager().getOnlinePlayers(assists);
    }

    private void incK(Player player) {
        UUID uuid = player.getUniqueId();
        PartyName partyName = game.getPlugin().getManager().getPlayerParty(uuid).getPartyName();
        if (killMap.containsKey(uuid)) {
            Integer n = killMap.get(uuid);
            killMap.put(uuid, n + 1);
        } else {
            killMap.put(uuid, 1);
        }

        if (partyKills.containsKey(partyName)) {
            Integer n = partyKills.get(partyName);
            partyKills.put(partyName, n + 1);
        } else {
            partyKills.put(partyName, 1);
        }

    }

    private void incD(Player player) {
        UUID uuid = player.getUniqueId();
        PartyName partyName = game.getPlugin().getManager().getPlayerParty(uuid).getPartyName();
        if (deathMap.containsKey(uuid)) {
            Integer n = deathMap.get(uuid);
            deathMap.put(uuid, n + 1);
        } else {
            deathMap.put(uuid, 1);
        }
        if (partyDeaths.containsKey(partyName)) {
            Integer n = partyDeaths.get(partyName);
            partyDeaths.put(partyName, n + 1);
        } else {
            partyDeaths.put(partyName, 1);
        }

    }

    private void incA(Player player) {
        UUID uuid = player.getUniqueId();
        PartyName partyName = game.getPlugin().getManager().getPlayerParty(uuid).getPartyName();
        if (assistMap.containsKey(uuid)) {
            Integer n = assistMap.get(uuid);
            assistMap.put(uuid, n + 1);
        } else {
            assistMap.put(uuid, 1);
        }
        if (partyAssists.containsKey(partyName)) {
            Integer n = partyAssists.get(partyName);
            partyAssists.put(partyName, n + 1);
        } else {
            partyAssists.put(partyName, 1);
        }
    }

    public int getPlayerKills(Player player) {
        return getPlayerKills(player.getUniqueId());
    }

    public int getPlayerKills(UUID player) {
        return killMap.getOrDefault(player, 0);
    }

    public int getPlayerDeaths(Player player) {
        return getPlayerDeaths(player.getUniqueId());
    }

    public int getPlayerDeaths(UUID player) {
        return deathMap.getOrDefault(player, 0);
    }

    public int getPlayerAssits(Player player) {
        return getPlayerAssits(player.getUniqueId());
    }

    public int getPlayerAssits(UUID player) {
        return assistMap.getOrDefault(player, 0);
    }

    public double getPlayerCausedDamage(Player player) {
        return getPlayerCausedDamage(player.getUniqueId());
    }

    public double getPlayerCausedDamage(UUID player) {
        return damageMap.getOrDefault(player, 0.00);
    }

    public double getPlayerDefendDamage(UUID player) {
        return defenceMap.getOrDefault(player, 0.00);
    }

    public double getPlayerDefendDamage(Player player) {
        return getPlayerDefendDamage(player.getUniqueId());
    }

    public int getPartyKills(Party party) {
        return partyKills.getOrDefault(party.getPartyName(), 0);
    }

    public int getPartyDeaths(Party party) {
        return partyDeaths.getOrDefault(party.getPartyName(), 0);
    }

    public int getPartyAssists(Party party) {
        return partyAssists.getOrDefault(party.getPartyName(), 0);
    }

    public double getPartyCausedDamage(Party party) {
        return partyDamages.getOrDefault(party.getPartyName(), 0.0);
    }

    public double getPartyDefendDamage(Party party) {
        return partyDefences.getOrDefault(party.getPartyName(), 0.0);
    }

    /**
     * 人数少，影响不大
     *
     * @return
     */
    public Party getMvpParty() {
        Party party = null;
        double maxPartyPerformance = -1;
        for (Party p : game.getGameParties().values()) {
            double performance = getPartyPerformance(p);
            if (performance > maxPartyPerformance) {
                maxPartyPerformance = performance;
                party = p;
            }
        }
        return party;
    }

    /**
     * 在游戏结束后获取队伍的 总结数据
     *
     * @param party 队伍
     * @return
     */
    public String getPartySummary(Party party) {
        String summary = game.getPlugin().getLocaleString("game.party-summary-pvp-game-finish", false);
        return summary
                .replace("%party_name%", party.getPartyName().toString() + party.getName())
                .replace("%damages%", String.format("%.2f", getPartyCausedDamage(party)))
                .replace("%kills%", String.valueOf(getPartyKills(party)))
                .replace("%deaths%", String.valueOf(getPartyDeaths(party)))
                .replace("%score%", String.format("%.2f", getPartyPerformance(party)));
    }

    /**
     * 在游戏结束后获取 玩家的 总结数据
     *
     * @param player
     * @return
     */
    public String getPlayerSummary(Player player) {
        //: "%player_name% 的数据：&6伤害%damages% &a击杀%kills% &e助攻%assits% &c死亡%deathes%"
        String summary = game.getPlugin().getLocaleString("game.player-summary-pvp-game-finish");
        return summary.replace("%player_name%", player.getName()).replace("%damages%", String.format("%.2f", getPlayerCausedDamage(player))).replace("%kills%", String.valueOf(getPlayerKills(player))).replace("%assits%", String.valueOf(getPlayerAssits(player))).replace("%deathes%", String.valueOf(getPlayerDeaths(player)));
    }

}
