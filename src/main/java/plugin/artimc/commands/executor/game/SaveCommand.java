package plugin.artimc.commands.executor.game;

import java.util.List;

import plugin.artimc.commands.context.CommandContext;

public class SaveCommand extends DefaultCommand {

    public SaveCommand(CommandContext context) {
        super(context);
    }

    @Override
    public List<String> suggest() {
        // game save xxxxxx
        if (!tryGetArg(1).isEmpty()) {
            return List.of();
        }

        if (!tryGetArg(0).isEmpty()) {
            return getPlugin().getConfigActiveGames();
        }

        return List.of();

    }

    @Override
    public boolean execute() {
        if (!hasPermission("artimc.game.admin"))
            throw new IllegalStateException(getLocaleString("no-permission"));

        if (tryGetArg(1).isBlank())
            throw new IllegalStateException(getLocaleString("error.missing-argument-when-save-game"));

        try {
            // 保存当前游戏
            getPlugin().saveGameConfiguration(tryGetArg(1));
            getPlayer().sendMessage(getLocaleString("command.setting-saved"));
            return true;
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }
}
