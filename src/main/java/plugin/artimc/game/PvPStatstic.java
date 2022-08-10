package plugin.artimc.game;

import java.util.*;

import org.bukkit.entity.Player;

import plugin.artimc.engine.Party;

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
    public PvPStatstic(PvPGame game) {
        this.game = game;
        damageMap = new HashMap<>();
        defenceMap = new HashMap<>();
        killMap = new HashMap<>();
        deathMap = new HashMap<>();
        assistMap = new HashMap<>();
        damageQueue = new LinkedList<>();
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

            if (!isEnermy(attacker, defencer))
                return;

            if (damageMap.containsKey(a)) {
                Double n = damageMap.getOrDefault(a, 0.00);
                damageMap.put(a, n + damage);
            } else {
                damageMap.put(a, damage);
            }

            if (defenceMap.containsKey(t)) {
                Double n = defenceMap.getOrDefault(t, 0.00);
                defenceMap.put(t, n + damage);
            } else {
                defenceMap.put(t, damage);
            }
            causeDamage(tick, attacker, defencer, damage);
        }
    }

    public void onDeath(int tick, Player player, Player killer) {
        if (player instanceof Player && killer instanceof Player) {
            UUID p = player.getUniqueId();
            UUID k = killer.getUniqueId();
            incK(k);
            incD(p);
            for (UUID a : getAssists(tick, k, p)) {
                incA(a);
            }
        }
    }

    private boolean isEnermy(Player attacker, Player target) {
        UUID a = attacker.getUniqueId();
        UUID t = target.getUniqueId();
        boolean sameParty = (game.getHostParty().contains(a) && game.getHostParty().contains(t))
                || (game.getGuestParty().contains(a) && game.getGuestParty().contains(t));
        return !sameParty;
    }

    private void causeDamage(int tick, Player attacker, Player defencer, Double damage) {
        updateDamagerQueueInTicks(tick);
        damageQueue.offer(new PvPDamage(tick, attacker.getUniqueId(), defencer.getUniqueId(), damage));
    }

    private void updateDamagerQueueInTicks(int tick) {
        if (damageQueue.isEmpty())
            return;
        while (damageQueue.peek() != null && damageQueue.peek().tick + contPeriod < tick) {
            damageQueue.poll();
        }
    }

    private List<UUID> getAssists(int tick, UUID killer, UUID deadPlayer) {
        List<UUID> assits = new ArrayList<>();
        for (PvPDamage d : damageQueue) {
            if (d.defencer.equals(deadPlayer) && (d.tick > tick - contPeriod) && !assits.contains(d.attacker)
                    && !d.attacker.equals(killer)) {
                assits.add(d.attacker);
            }
        }
        return assits;
    }

    private void incK(UUID uuid) {
        if (killMap.containsKey(uuid)) {
            Integer n = killMap.get(uuid);
            killMap.put(uuid, n + 1);
        } else {
            killMap.put(uuid, 1);
        }
    }

    private void incD(UUID uuid) {
        if (deathMap.containsKey(uuid)) {
            Integer n = deathMap.get(uuid);
            deathMap.put(uuid, n + 1);
        } else {
            deathMap.put(uuid, 1);
        }
    }

    private void incA(UUID uuid) {
        if (assistMap.containsKey(uuid)) {
            Integer n = assistMap.get(uuid);
            assistMap.put(uuid, n + 1);
        } else {
            assistMap.put(uuid, 1);
        }
    }

    public int getPlayerKills(Player player) {
        return getPlayerKills(player.getUniqueId());
    }

    public int getPlayerKills(UUID player) {
        return killMap.getOrDefault(player, 0);
    }

    public int getPlayerDeathes(Player player) {
        return getPlayerDeathes(player.getUniqueId());
    }

    public int getPlayerDeathes(UUID player) {
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
        int ret = 0;
        for (UUID uuid : party.getPlayers()) {
            ret += getPlayerKills(uuid);
        }
        return ret;
    }

    public float getPartyCausedDamage(Party party) {
        double ret = 0;
        for (UUID uuid : party.getPlayers()) {
            ret += getPlayerCausedDamage(uuid);
        }
        return (float) ret;
    }

    public float getPartyDefendDamage(Party party) {
        double ret = 0;
        for (UUID uuid : party.getPlayers()) {
            ret += getPlayerDefendDamage(uuid);
        }
        return (float) ret;
    }

    public Party getMvpParty(){
        Party party = null;
        float partyScore = -1;
        for(Party p:game.getGameParties().values()){
            float party_damage = getPartyCausedDamage(p);
            int party_kills = getPartyKills(p);
            float score = party_damage / 10 * party_kills;
            if(score > partyScore){
                partyScore = score;
                party = p;
            }
        };
        return party;
    }

    /**
     * 在游戏结束后获取队伍的 总结数据
     * @param party 队伍
     * @return
     */
    public String getPartySummary(Party party){
        String summary = game.getPlugin().getLocaleString("game.party-summary-pvp-game-finish",false);
        return summary.replace("%party_name%", party.getPartyName().toString() +  party.getName())
                .replace("%damages%",String.format("%.2f", getPartyCausedDamage(party)))
                .replace("%kills%",String.valueOf(getPartyKills(party)))
                .replace("%defence%",String.format("%.2f",getPartyDefendDamage(party)));
    }

    /**
     * 在游戏结束后获取 玩家的 总结数据
     * @param player
     * @return
     */
    public String getPlayerSummary(Player player){
        //: "%player_name% 的数据：&6伤害%damages% &a击杀%kills% &e助攻%assits% &c死亡%deathes%"
        String summary = game.getPlugin().getLocaleString("game.player-summary-pvp-game-finish");
        return summary.replace("%player_name%",player.getName())
                .replace("%damages%",String.format("%.2f", getPlayerCausedDamage(player)))
                .replace("%kills%",String.valueOf(getPlayerKills(player)))
                .replace("%assits%",String.valueOf(getPlayerAssits(player)))
                .replace("%deathes%",String.valueOf(getPlayerDeathes(player)));

    }

}
