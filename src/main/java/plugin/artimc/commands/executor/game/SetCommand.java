package plugin.artimc.commands.executor.game;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.mode.ConfigMode;
import plugin.artimc.utils.PlayerUtil;

public class SetCommand extends DefaultCommand {

    public SetCommand(CommandContext context) {
        super(context);
    }

    @Override
    public List<String> suggest() {
        // game set #game set.spawn 120
        // game set #game mode add/remove #mode_name

        if (!tryGetArg(3).isEmpty()
                && tryGetArg(3).equals(getParamsString("mode-action.remove"))) {
            return getPlugin().getGameConfigurations().get(tryGetArg(1)).getStringList("modes.list");
        }

        if (!tryGetArg(2).isEmpty()) {
            if (tryGetArg(2).equals(getParamsString("set.mode"))) {
                return List.of(getParamsString("mode-action.add"), getParamsString("mode-action.remove"));
            }
            return List.of();
        }

        if (!tryGetArg(1).isEmpty()) {
            return List.of(getParamsString("set.default-spawn"), getParamsString("set.guest-spawn"), getParamsString("set.host-spawn"), getParamsString("set.wait-period"), getParamsString("set.game-period"), getParamsString("set.mode"), getParamsString("set.finish-period"));
        }

        if (!tryGetArg(0).isEmpty()) {
            return getPlugin().getConfigActiveGames();
        }

        return List.of();
    }

    @Override
    public boolean execute() {

        // game set #game default-spawn
        // game set #game inventory add/remove #name
        if (!hasPermission("artimc.game.admin")) throw new IllegalStateException(getLocaleString("no-permission"));

        if (tryGetArg(1).isBlank()) // game
            throw new IllegalStateException(getLocaleString("error.missing-argument-game"));

        if (tryGetArg(2).isBlank()) // setting
            throw new IllegalStateException(getLocaleString("error.missing-argument-when-set-game"));

        YamlConfiguration gameConfiguration = getGameConfiguration(tryGetArg(1));
        if (gameConfiguration == null)
            throw new IllegalStateException(getLocaleString("command.game-unavaliable").replace("%game%", tryGetArg(1)));

        Player player = getPlayer();
        Location loc = player.getLocation();
        World world = player.getWorld();
        String successMessage = getLocaleString("command.setting-changed", true).replace("%setting%", tryGetArg(2));
        String missingSettingValue = getLocaleString("error.missing-argument-setting-value").replace("%setting%", tryGetArg(2));
        // 默认出生点
        if (tryGetArg(2).equals(getParamsString("set.default-spawn"))) {
            gameConfiguration.set("spawn.default.world", world.getName());
            gameConfiguration.set("spawn.default.x", loc.getBlockX() + 0.5);
            gameConfiguration.set("spawn.default.y", loc.getBlockY());
            gameConfiguration.set("spawn.default.z", loc.getBlockZ() + 0.5);
            gameConfiguration.set("spawn.default.yaw", (Math.round(loc.getYaw() / 90)));
            gameConfiguration.set("spawn.default.pitch", (Math.round(loc.getPitch() / 90)));
            getPlayer().sendMessage(successMessage);
            return true;
        }
        // 主队出生点
        else if (tryGetArg(2).equals(getParamsString("set.host-spawn"))) {
            gameConfiguration.set("spawn.host.world", world.getName());
            gameConfiguration.set("spawn.host.x", loc.getBlockX() + 0.5);
            gameConfiguration.set("spawn.host.y", loc.getBlockY());
            gameConfiguration.set("spawn.host.z", loc.getBlockZ() + 0.5);
            gameConfiguration.set("spawn.host.yaw", (Math.round(loc.getYaw() / 90)));
            gameConfiguration.set("spawn.host.pitch", (Math.round(loc.getPitch() / 90)));
            getPlayer().sendMessage(successMessage);
            return true;
        }
        // 客队出生点
        else if (tryGetArg(2).equals(getParamsString("set.guest-spawn"))) {
            gameConfiguration.set("spawn.guest.world", world.getName());
            gameConfiguration.set("spawn.guest.x", loc.getBlockX() + 0.5);
            gameConfiguration.set("spawn.guest.y", loc.getBlockY());
            gameConfiguration.set("spawn.guest.z", loc.getBlockZ() + 0.5);
            gameConfiguration.set("spawn.guest.yaw", (Math.round(loc.getYaw() / 90)));
            gameConfiguration.set("spawn.guest.pitch", (Math.round(loc.getPitch() / 90)));
            getPlayer().sendMessage(successMessage);
            return true;
        }
        // 等待时间
        else if (tryGetArg(2).equals(getParamsString("set.wait-period"))) {
            // game set #game wait-period 120
            if (tryGetArg(3).isBlank()) throw new IllegalStateException(missingSettingValue);
            try {
                gameConfiguration.set("period.wait", Integer.valueOf(tryGetArg(3)));
                getPlayer().sendMessage(successMessage);
                return true;
            } catch (NumberFormatException ex) {
                throw new IllegalStateException(getLocaleString("error.invalid-number", false));
            }
        }
        // 游戏时间
        else if (tryGetArg(2).equals(getParamsString("set.game-period"))) {
            // game set #game game-period 120
            if (tryGetArg(3).isBlank()) throw new IllegalStateException(missingSettingValue);
            try {
                gameConfiguration.set("period.game", Integer.valueOf(tryGetArg(3)));
                getPlayer().sendMessage(successMessage);
                return true;
            } catch (NumberFormatException ex) {
                throw new IllegalStateException(getLocaleString("error.invalid-number", false));
            }
        }
        // 结算时间
        else if (tryGetArg(2).equals(getParamsString("set.finish-period"))) {
            // game set #game finish-period 120
            if (tryGetArg(3).isBlank()) throw new IllegalStateException(missingSettingValue);
            try {
                gameConfiguration.set("period.finish", Integer.valueOf(tryGetArg(3)));
                getPlayer().sendMessage(successMessage);
                return true;
            } catch (NumberFormatException ex) {
                throw new IllegalStateException(getLocaleString("error.invalid-number", false));
            }
        }
        // 游戏模式
        else if (tryGetArg(2).equals(getParamsString("set.mode"))) {
            // game set #game mode add/remove #mode_name
            String gameName = tryGetArg(1);
            String action = tryGetArg(3);
            String mode = tryGetArg(4);
            YamlConfiguration gameConfig = getPlugin().getGameConfigurations().get(gameName);
            // 将玩家当前的物品栏物品设置为当前规则的物品
            if (action.equals(getParamsString("mode-action.add")) && !mode.isEmpty()) {
                List<String> modeList = gameConfig.getStringList("modes.list");
                if (!modeList.contains(mode)) {
                    modeList.add(mode);
                    gameConfig.set("modes.list", modeList);
                }
                gameConfig.set("modes." + mode, PlayerUtil.serializerInventory(getPlayer()));
                getPlayer().sendMessage(successMessage);
                return true;
            }
            // 移除指定的游戏模式
            else if (action.equals(getParamsString("mode-action.remove")) && !mode.isEmpty()) {
                List<String> modeList = gameConfig.getStringList("modes.list");
                if (!modeList.contains(mode))
                    throw new IllegalStateException(ChatColor.GRAY + "不存在模式：" + mode);
                modeList.remove(mode);
                gameConfig.set("modes.list", modeList);
                gameConfig.set("modes." + mode, null);
                getPlayer().sendMessage(successMessage);
                return true;
            }
        }
        throw new IllegalStateException(getLocaleString("error.unsupport-settings"));
    }

}
