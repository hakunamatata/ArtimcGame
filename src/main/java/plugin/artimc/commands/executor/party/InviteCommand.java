package plugin.artimc.commands.executor.party;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Party;

/**
 * 邀请玩家加入队伍
 */
public class InviteCommand extends DefaultCommand {

    public InviteCommand(CommandContext context) {
        super(context);
    }

    @Override
    public List<String> suggest() {

        Party party = getParty();

        List<String> matchedPlayers = new ArrayList<>();
        for (Player player : getPlayer().getServer().getOnlinePlayers()) {
            String name = player.getName();
            // 自己除外
            if (!name.equalsIgnoreCase(getPlayer().getName())) {
                // 不是自己，但如果已经在队伍中的成员也除外
                if (party != null && party.contains(player.getUniqueId()))
                    continue;
                matchedPlayers.add(name);
            }
        }
        Collections.sort(matchedPlayers, String.CASE_INSENSITIVE_ORDER);
        return matchedPlayers;
    }

    @Override
    public boolean execute() {
        Party party = getParty();

        // 如果自身没有队伍
        // 1. 创建队伍
        // 2. 邀请玩家
        // 如果自身有队伍
        // 1. 邀请玩家
        String inviteeName = tryGetArg(1);
        if (inviteeName.isBlank())
            throw new IllegalStateException(getLocaleString("command.missing-player-name"));

        Player invitee = getPlugin().getServer().getPlayerExact(inviteeName);

        // 玩家不在线
        if (invitee == null) {
            getPlayer().sendMessage(getLocaleString("player-offline").replace("%player_name%", inviteeName));
            return false;
        }

        if (invitee.getUniqueId().equals(getPlayer().getUniqueId())) {
            throw new IllegalStateException(getLocaleString("command.cant-invite-urself"));
        }

        if (party == null)
            party = new Party(getPlayer(), getPlugin());

        if (party.willOverload())
            throw new IllegalStateException(getLocaleString("command.invite-party-will-overload")
                    .replace("%limit%", getPlugin().getConfig().getString("settings.max-player", "5")));

        if (party.invite(invitee)) {
            String partyOwner = party.getOwnerName();
            String broadcastInviting = getLocaleString("command.broadcast-inviting")
                    .replace("%party_owner%", partyOwner)
                    .replace("%invitee%", inviteeName);
            // 队伍内广播邀请
            party.sendMessage(Component.text(broadcastInviting));

            String inviteeGotMessage = getLocaleString("command.invitee-got-message")
                    .replace("%party_owner%", partyOwner);
            String accept = getLocaleString("accept", false);
            String inviteeAcceptSuggest = getLocaleString("command.invitee-accept-suggest", false)
                    .replace("%party_owner%", partyOwner);
            // 被邀请的玩家发送邀请信息
            invitee.sendMessage(Component.text(inviteeGotMessage)
                    .append(Component.text()
                            .content(accept)
                            .hoverEvent(HoverEvent.showText(Component.text(inviteeAcceptSuggest)))
                            .clickEvent(ClickEvent.runCommand("/party " + getCommandString("join") + " " + partyOwner))
                            .build()));
        }
        return true;
    }
}
