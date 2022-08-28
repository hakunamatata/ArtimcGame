package plugin.artimc.commands.executor.party;

import java.util.List;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.commands.context.PartyCommandContext;
import plugin.artimc.commands.executor.CommandExecutor;
import plugin.artimc.engine.Party;

public class DefaultCommand extends CommandExecutor {

    public DefaultCommand(CommandContext context) {
        super(context);
    }

    protected String getCommandString(String path) {
        return getContext().getCommandConfiguration().getString(path);
    }

    protected String getParamsString(String path) {
        return getContext().getParameterConfiguration().getString(path);
    }

    @Override
    public PartyCommandContext getContext() {
        return (PartyCommandContext) super.getContext();
    }

    @Override
    public List<String> suggest() {

        Player player = getPlayer();
        Party party = getParty();
        // 玩家没有队伍，允许执行邀请其他玩家加入队伍
        if (party == null)
            return List.of(getCommandString("invite"),
                    getCommandString("create"));

        // 如果玩家有队伍，允许离开队伍
        // 如果玩家是队长，允许队长操作
        if (party.isOwner(player)) {
            return List.of(
                    getCommandString("invite"),
                    getCommandString("dismiss"),
                    getCommandString("kick"),
                    getCommandString("transfer"),
                    getCommandString("list"),
                    getCommandString("rename"),
                    getCommandString("set"));
        } else {
            return List.of(
                    getCommandString("leave"),
                    getCommandString("list"));
        }
    }

    @Override
    public boolean execute() {
        List<String> commands = Lists.newArrayList(suggest());
        commands.add(getCommandString("join"));
        if (getContext().getArgs().length == 0) {
            getPlayer().sendMessage(getLocaleString("command.party-usage"));
        }
        return true;
    }
}
