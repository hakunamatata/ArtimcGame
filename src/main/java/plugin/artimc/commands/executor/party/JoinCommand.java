package plugin.artimc.commands.executor.party;

import java.util.List;

import org.bukkit.OfflinePlayer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Party;

public class JoinCommand extends DefaultCommand {

    public JoinCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {

        String ownerName = tryGetArg(1);
        if (ownerName.isBlank())
            throw new IllegalStateException(getLocaleString("command.party-join-usage"));

        if (getParty() != null) {
            getPlayer().sendMessage(
                    Component.text(getLocaleString("command.ur-already-in-a-party"))
                            .append(
                                    Component.text()
                                            .content(getLocaleString("command.leave-party-text", false))
                                            .clickEvent(ClickEvent.runCommand("/party " + getCommandString("leave")))
                                            .hoverEvent(HoverEvent.showText(Component.text(
                                                    getLocaleString("command.leave-party-suggest",
                                                            false).replace("%party_owner%", ownerName))))
                                            .build()));
            return false;
        }

        OfflinePlayer partyOwner = getOfflinePlayer(ownerName);
        Party party = partyOwner != null ? getManager().getPlayerParty(partyOwner.getUniqueId()) : null;

        // 你要加入的队伍不存在
        if (party == null)
            throw new IllegalStateException(getLocaleString("command.party-not-exist"));

        if (!party.isInvited(getPlayer().getUniqueId()))
            throw new IllegalStateException(getLocaleString("command.party-not-invite-you"));

        if (party.willOverload())
            throw new IllegalStateException(getLocaleString("command.join-party-will-overload")
                    .replace("%limit%", getPlugin().getConfig().getString("settings.max-player", "5"))
                    .replace("%party_owner%", ownerName));

        party.join(getPlayer());
        party.sendMessage(Component.text(getLocaleString("command.player-join-party")
                .replace("%player_name%", ownerName)));

        // 后面处理游戏逻辑

        return super.execute();
    }

    @Override
    public List<String> suggest() {
        // TODO Auto-generated method stub
        return super.suggest();
    }

}
