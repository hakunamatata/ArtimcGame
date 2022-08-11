package plugin.artimc.commands.executor.game;

import java.util.List;

import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.game.PvPGame;

public class RuleCommand extends DefaultCommand {

    public RuleCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        // ------0-----1----2-------3---
        // game rule item #附魔金 #enable
        // game rule friendlyFire #enable

        // game move player host/guest
        /**
         * Checkings
         */
        if (tryGetArg(1).isBlank()) {
            getPlayer().sendMessage(getLocaleString("game.usage-rule"));
            return false;
        }

        if (getGame() == null)
            throw new IllegalStateException(getLocaleString("error.ur-not-in-game"));

        if (getGame() instanceof PvPGame) {
            PvPGame pvpGame = (PvPGame) getGame();

            if (pvpGame.isGaming())
                throw new IllegalStateException(getLocaleString("game.rule-err-game-is-gaming"));

            if (!pvpGame.getHostParty().contains(getPlayer().getUniqueId()))
                throw new IllegalStateException(getLocaleString("error.party-not-host"));

            // 禁用物品的规则
            if (tryGetArg(1).equals(getParamsString("rule.item"))) {
                String item = tryGetArg(2);
                String state = tryGetArg(3);
                if (item.isBlank() || state.isEmpty())
                    throw new IllegalStateException(getLocaleString("error.unknown-setting"));
                if (state.equals(getParamsString("rule.enable"))) {
                    pvpGame.getPvpItemController().unban(item);
                    pvpGame.setPartyReady(pvpGame.getHostParty(), false);
                    pvpGame.setPartyReady(pvpGame.getGuestParty(), false);
                    pvpGame.sendMessage(getLocaleString("game.rule-changed-unban-item").replace("%item%", item));
                    pvpGame.sendMessage(getLocaleString("game.rule-changed-parties-unready"));
                    return true;
                } else if (state.equals(getParamsString("rule.disable"))) {
                    pvpGame.getPvpItemController().ban(item);
                    pvpGame.setPartyReady(pvpGame.getHostParty(), false);
                    pvpGame.setPartyReady(pvpGame.getGuestParty(), false);
                    pvpGame.sendMessage(getLocaleString("game.rule-changed-ban-item", false).replace("%item%", item));
                    pvpGame.sendMessage(getLocaleString("game.rule-changed-parties-unready", false));
                    return true;
                }
            } else if (tryGetArg(1).equals(getParamsString("rule.friendlyFire"))) {
                String state = tryGetArg(2);
                if (state.isBlank())
                    throw new IllegalStateException(getLocaleString("error.missing-argument"));
                if (state.equals(getParamsString("rule.enable"))) {
                    // 允许队友误伤
                    pvpGame.setFriendlyFire(true);
                    pvpGame.setPartyReady(pvpGame.getHostParty(), false);
                    pvpGame.setPartyReady(pvpGame.getGuestParty(), false);
                    pvpGame.sendMessage(
                            getLocaleString("game.rule-changed-friendly-fire-on", false).replace("%player_name%",
                                    getPlayer().getName()));
                    pvpGame.sendMessage(getLocaleString("game.rule-changed-parties-unready", false));
                    return true;
                } else if (state.equals(getParamsString("rule.disable"))) {
                    // 禁止队友误伤
                    pvpGame.setFriendlyFire(false);
                    pvpGame.setPartyReady(pvpGame.getHostParty(), false);
                    pvpGame.setPartyReady(pvpGame.getGuestParty(), false);
                    pvpGame.sendMessage(
                            getLocaleString("game.rule-changed-friendly-fire-off", false).replace("%player_name%",
                                    getPlayer().getName()));
                    pvpGame.sendMessage(getLocaleString("game.rule-changed-parties-unready", false));
                    return true;
                }
            }
        }
        throw new IllegalStateException(getLocaleString("error.unsupport-settings"));
    }

    @Override
    public List<String> suggest() {
        // ------0-----1----2-------3---
        // game rule item #附魔金 #enable
        // game rule friendlyFire #enable
        if (!tryGetArg(3).isBlank()) {
            return List.of();
        } else if (!tryGetArg(2).isBlank()) {
            return List.of(getParamsString("rule.enable"),
                    getParamsString("rule.disable"));
        } else if (!tryGetArg(1).isBlank()) {
            if (tryGetArg(1).equals(getParamsString("rule.item")))
                return getParamsStringList("rule.items");
            else if (tryGetArg(1).equals(getParamsString("rule.friendlyFire")))
                return List.of(getParamsString("rule.enable"),
                        getParamsString("rule.disable"));
        } else if (!tryGetArg(0).isBlank()) {
            return List.of(
                    getParamsString("rule.item"),
                    getParamsString("rule.friendlyFire"));
        }
        return List.of();
    }

}
