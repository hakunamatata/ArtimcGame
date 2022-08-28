package plugin.artimc.commands.executor.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.commands.context.GameCommandContext;
import plugin.artimc.commands.executor.CommandExecutor;
import plugin.artimc.engine.IGame;
import plugin.artimc.game.PvPGame;

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

    protected List<String> getParamsStringList(String path) {
        return getContext().getParameterConfiguration().getStringList(path);
    }

    protected YamlConfiguration getGameConfiguration(String game) {
        if (game.isEmpty()) return null;
        if (getPlugin().getGameConfigurations() == null) return null;
        return getPlugin().getGameConfigurations().get(game);
    }

    @Override
    protected String getLocaleString(String path) {
        return getPlugin().getLocaleString(path, true);
    }

    @Override
    protected String getLocaleString(String path, boolean prefix) {
        IGame game = getGame();
        String prefixString = "";
        if (prefix) {
            if (game == null) prefixString = super.getLocaleString("prefix-default-game", false);
            else prefixString = super.getLocaleString("prefix-game", false).replace("%game%", game.getName());
        }
        return prefixString + super.getLocaleString(path, false);
    }

    @Override
    public GameCommandContext getContext() {
        return (GameCommandContext) super.getContext();
    }

    @Override
    public List<String> suggest() {
        IGame game = getGame();
        List<String> list = new ArrayList<>();
        // 如果具有管理权限
        if (hasPermission("artimc.game.admin"))
            list.addAll(List.of(getCommandString("set"), getCommandString("save"), getCommandString("reload")));
        // 当前没有正在进行的游戏
        if (game == null) {
            list.add(getCommandString("join"));
        }
        // 如果当前有正在进行的游戏
        else {
            list.add(getCommandString("leave"));
            // 如果是 PvP 游戏
            if (game instanceof PvPGame && !game.isGaming()) {
                list.add(getCommandString("rule"));
                // 补全提示，如果不是队长，不显示准备提示
                if (getParty() != null && getParty().isOwner(getPlayer())) {
                    list.add(getCommandString("ready"));
                    list.add(getCommandString("unready"));
                    // 主队队长显示 移动成员的提示
                    PvPGame pvpGame = (PvPGame) game;
                    if (pvpGame.getHostParty() != null && pvpGame.getHostParty().equals(getParty())) {
                        list.add(getCommandString("move"));
                    }
                }
            }
        }
        return list;
    }

    @Override
    public boolean execute() {
        if (getContext().getArgs().length == 0) {
            getPlayer().sendMessage(getLocaleString("command.game-usage"));
        }
        return true;
    }

}
