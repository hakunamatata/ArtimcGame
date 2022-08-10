package plugin.artimc.commands.executor.partymessage;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import plugin.artimc.ArtimcGameManager;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.commands.executor.CommandExecutor;
import plugin.artimc.engine.Party;

public class DefaultCommand extends CommandExecutor {

    public DefaultCommand(CommandContext context) {
        super(context);
    }

    @Override
    public List<String> suggest() {
        return Lists.newArrayList();
    }

    @Override
    public boolean execute() {
        Player player = getPlayer();
        Party party = getParty();
        if (party == null) {
            player.sendMessage(getLocaleString("command.ur-not-in-a-party-when-message", false));
            return false;
        }

        if (tryGetArg(0).isBlank()) {
            // 等效于玩家输入的是 "/pm or /partymessage"
            ArtimcGameManager manager = getManager();
            UUID p = player.getUniqueId();
            if (manager.isPlayerEnabledPartyChannel(p)) {
                manager.disablePartyChannel(p);
                player.sendMessage(getLocaleString("command.disable-party-channel"));
            } else {
                manager.enablePartyChannel(p);
                player.sendMessage(getLocaleString("command.enable-party-channel"));
            }
        } else {
            party.chat(player, String.join(" ", getContext().getArgs()));
        }
        return true;
    }

}
