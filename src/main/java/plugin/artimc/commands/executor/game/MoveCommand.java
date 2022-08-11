package plugin.artimc.commands.executor.game;

import org.bukkit.entity.Player;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Party;
import plugin.artimc.game.PvPGame;

import java.util.List;

public class MoveCommand extends DefaultCommand {

    public MoveCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        // game move #player #host/guest

        /**
         * Checkings
         */
        if (getGame() == null) throw new IllegalStateException(getLocaleString("error.ur-not-in-game"));

        // 游戏内必须要队伍
        if (getParty() == null) throw new IllegalStateException(getLocaleString("command.ur-not-in-a-party"));

        // 必须是队长才能准备
        if (!getParty().isOwner(getPlayer()))
            throw new IllegalStateException(getLocaleString("command.ur-not-party-owner"));

        // 必须有要移动的玩家名
        if (tryGetArg(1).isBlank()) throw new IllegalStateException(getLocaleString("error.missing-argument"));

        if (tryGetArg(2).isBlank()) throw new IllegalStateException(getLocaleString("error.missing-argument"));

        // 主队队长不能移动自己
        if (tryGetArg(1).equals(getPlayer().getName()))
            throw new IllegalStateException(getLocaleString("error.cant-move-urself"));

        if (getGame() instanceof PvPGame) {
            PvPGame game = (PvPGame) getGame();
            if (game.getHostParty() == null || !game.getHostParty().isOwner(getPlayer()))
                throw new IllegalStateException(getLocaleString("error.party-not-host"));

            Party targetParty = null;
            Player movee = getManager().getPlugin().getServer().getPlayer(tryGetArg(1));
            Party moveeParty = getManager().getPlayerParty(movee);

            if (tryGetArg(2).equals(getParamsString("party.host"))) targetParty = game.getHostParty();
            else if (tryGetArg(2).equals(getParamsString("party.guest"))) targetParty = game.getGuestParty();

            if (game.isGaming())
                throw new IllegalStateException(getLocaleString("game.move-err-game-is-running"));

            // 你要移动的玩家是其他队伍的队长
            if (moveeParty.isOwner(movee))
                throw new IllegalStateException(getLocaleString("game.move-err-player-is-party-owner").replace("%player_name%", movee.getName()).replace("%party_name%", moveeParty.getName()));

            game.movePlayer(movee, targetParty);

        }


        return super.execute();
    }

    @Override
    public List<String> suggest() {
        // game move #player #host/guest
        try {
            PvPGame game = (PvPGame) getGame();
            if (game.getHostParty() != null && game.getHostParty().isOwner(getPlayer())) {

                if (!tryGetArg(1).isBlank()) {
                    return List.of(getParamsString("party.host"), getParamsString("party.guest"));
                }

                if (!tryGetArg(0).isBlank()) {
                    return game.getOnlinePlayers().stream().map(player -> player.getName()).filter(name -> !name.equals(getPlayer().getName())).toList();
                }
            }
        } catch (Exception ex) {
            return super.suggest();
        }
        return List.of();
    }
}
