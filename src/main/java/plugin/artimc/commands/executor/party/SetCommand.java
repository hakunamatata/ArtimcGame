package plugin.artimc.commands.executor.party;

import java.util.List;

import org.bukkit.entity.Player;

import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Party;

public class SetCommand extends DefaultCommand {

    public SetCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        Player player = getPlayer();
        Party party = getParty();
        if (tryGetArg(1).isBlank())
            throw new IllegalStateException(
                    getLocaleString("error.unknown-setting"));

        if (party == null)
            throw new IllegalStateException(getLocaleString("command.ur-not-in-a-party"));

        if (!party.isOwner(player))
            throw new IllegalStateException(getLocaleString("command.ur-not-party-owner"));

        String partyOwnerName = party.getOwnerName();
        if (tryGetArg(1).equals(getParamsString("set.friendlyFire"))) {
            if (tryGetArg(2).equals(getLocaleString("enable", false))) {
                party.setFriendlyFire(true);
                party.sendMessage(getLocaleString("command.setting-friendly-fire-on").replace("%party_owner%",
                        partyOwnerName));
            } else if (tryGetArg(2).equals(getLocaleString("disable", false))) {
                party.setFriendlyFire(false);
                party.sendMessage(getLocaleString("command.setting-friendly-fire-off").replace("%party_owner%",
                        partyOwnerName));
            } else {
                if (party.isFriendlyFire()) {
                    party.setFriendlyFire(false);
                    party.sendMessage(getLocaleString("command.setting-friendly-fire-off").replace("%party_owner%",
                            partyOwnerName));
                } else {
                    party.setFriendlyFire(true);
                    party.sendMessage(getLocaleString("command.setting-friendly-fire-on").replace("%party_owner%",
                            partyOwnerName));
                }
            }
        }
        return false;
    }

    @Override
    public List<String> suggest() {
        // party 设置 队友误伤
        if (tryGetArg(1).equals(getParamsString("set.friendlyFire"))) {
            return List.of(
                    getLocaleString("enable", false),
                    getLocaleString("disable", false));
        }
        // party 设置
        return List.of(getParamsString("set.friendlyFire"));
    }

}
