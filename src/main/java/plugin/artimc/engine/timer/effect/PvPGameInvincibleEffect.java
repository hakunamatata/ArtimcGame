package plugin.artimc.engine.timer.effect;

import org.bukkit.entity.Player;
import plugin.artimc.engine.GameRunnable;
import plugin.artimc.game.PvPGame;

public class PvPGameInvincibleEffect extends PlayerEffect {

    public PvPGameInvincibleEffect(Player player, int period, GameRunnable game) {
        super(PVP_GAME_RESPAWN_INVINCIBLE, player, period, game);
    }

    @Override
    protected void onStart() {
        if (getGame() instanceof PvPGame) {
            PvPGame game = (PvPGame) getGame();
            game.setPlayerInvincible(getPlayer(), true);
            getPlayer().setGlowing(true);
            game.log(String.format("%s 被赋予了 %s 秒 无敌 效果", getPlayer().getName(), getPeriod()));
        }
        super.onStart();
    }

    @Override
    protected void onFinish() {
        if (getGame() instanceof PvPGame) {
            PvPGame game = (PvPGame) getGame();
            game.setPlayerInvincible(getPlayer(), false);
            getPlayer().setGlowing(false);
            game.log(String.format("%s 被解除了 无敌 效果", getPlayer().getName(), getPeriod()));
        }
        super.onFinish();
    }
}
