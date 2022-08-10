package plugin.artimc.game;

import java.util.UUID;

public class PvPDamage {

    public final int tick;
    public final UUID attacker;
    public final UUID defencer;
    public final Double damage;

    public PvPDamage(int tick, UUID attacker, UUID defencer, Double damage) {
        this.tick = tick;
        this.attacker = attacker;
        this.defencer = defencer;
        this.damage = damage;
    }

    public int getTick() {
        return tick;
    }

    public UUID getAttacker() {
        return attacker;
    }

    public UUID getEntity() {
        return defencer;
    }

    public Double getDamage() {
        return damage;
    }

}
