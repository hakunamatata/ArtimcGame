package plugin.artimc.engine;

import org.bukkit.entity.Player;
import plugin.artimc.common.AbstractComponent;

/**
 * Player Name Tag Manager
 */
public class NameTagManager extends AbstractComponent {
    private final IGame game;

    public NameTagManager(IGame game) {
        super(game.getPlugin());
        this.game = game;
    }

    public IGame getGame() {
        return game;
    }

    protected void applyFor(Player identity) {

    }

    protected void resetFor(Player identity) {

    }

    public void apply() {
        for (Player identity : game.getOnlinePlayers()) {
            applyFor(identity);
        }
    }

    public void reset(Player identity) {
        resetFor(identity);
    }

    public void clear() {
        game.getOnlinePlayers().forEach(identity -> {
            reset(identity);
        });
    }
}
