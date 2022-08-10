package plugin.artimc.commands.executor.game;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import plugin.artimc.commands.context.CommandContext;

public class SetCommand extends DefaultCommand {

    public SetCommand(CommandContext context) {
        super(context);
    }

    @Override
    public List<String> suggest() {
        // game set #game set.spawn 120

        if (!tryGetArg(2).isEmpty()) {
            return List.of();
        }

        if (!tryGetArg(1).isEmpty()) {
            return List.of(
                    getParamsString("set.default-spawn"),
                    getParamsString("set.guest-spawn"),
                    getParamsString("set.host-spawn"),
                    getParamsString("set.wait-period"),
                    getParamsString("set.game-period"),
                    getParamsString("set.finish-period"));
        }

        if (!tryGetArg(0).isEmpty()) {
            return getPlugin().getConfigActiveGames();
        }

        return List.of();
    }

    @Override
    public boolean execute() {

        // game set #game default-spawn

        if (!hasPermission("artimc.game.admin"))
            throw new IllegalStateException(getLocaleString("no-permission"));

        if (tryGetArg(1).isBlank()) // game
            throw new IllegalStateException(getLocaleString("error.missing-argument-game"));

        if (tryGetArg(2).isBlank()) // setting
            throw new IllegalStateException(getLocaleString("error.missing-argument-when-set-game"));

        YamlConfiguration gameConfiguration = getGameConfiguration(tryGetArg(1));
        if (gameConfiguration == null)
            throw new IllegalStateException(
                    getLocaleString("command.game-unavaliable").replace("%game%", tryGetArg(1)));

        Player player = getPlayer();
        Location loc = player.getLocation();
        World world = player.getWorld();
        String successMessage = getLocaleString("command.setting-changed", true).replace("%setting%", tryGetArg(2));
        String missingSettingValue = getLocaleString("error.missing-argument-setting-value").replace("%setting%",
                tryGetArg(2));
        if (tryGetArg(2).equals(getParamsString("set.default-spawn"))) {
            gameConfiguration.set("spawn.default.world", world.getName());
            gameConfiguration.set("spawn.default.x", loc.getBlockX() + 0.5);
            gameConfiguration.set("spawn.default.y", loc.getBlockY());
            gameConfiguration.set("spawn.default.z", loc.getBlockZ() + 0.5);
            gameConfiguration.set("spawn.default.yaw", (Math.round(loc.getYaw() / 90)));
            gameConfiguration.set("spawn.default.pitch", (Math.round(loc.getPitch() / 90)));
            getPlayer().sendMessage(successMessage);
            return true;
        } else if (tryGetArg(2).equals(getParamsString("set.host-spawn"))) {
            gameConfiguration.set("spawn.host.world", world.getName());
            gameConfiguration.set("spawn.host.x", loc.getBlockX() + 0.5);
            gameConfiguration.set("spawn.host.y", loc.getBlockY());
            gameConfiguration.set("spawn.host.z", loc.getBlockZ() + 0.5);
            gameConfiguration.set("spawn.host.yaw", (Math.round(loc.getYaw() / 90)));
            gameConfiguration.set("spawn.host.pitch", (Math.round(loc.getPitch() / 90)));
            getPlayer().sendMessage(successMessage);
            return true;
        } else if (tryGetArg(2).equals(getParamsString("set.guest-spawn"))) {
            gameConfiguration.set("spawn.guest.world", world.getName());
            gameConfiguration.set("spawn.guest.x", loc.getBlockX() + 0.5);
            gameConfiguration.set("spawn.guest.y", loc.getBlockY());
            gameConfiguration.set("spawn.guest.z", loc.getBlockZ() + 0.5);
            gameConfiguration.set("spawn.guest.yaw", (Math.round(loc.getYaw() / 90)));
            gameConfiguration.set("spawn.guest.pitch", (Math.round(loc.getPitch() / 90)));
            getPlayer().sendMessage(successMessage);
            return true;
        } else if (tryGetArg(2).equals(getParamsString("set.wait-period"))) {
            // game set #game wait-period 120
            if (tryGetArg(3).isBlank())
                throw new IllegalStateException(missingSettingValue);
            try {
                gameConfiguration.set("period.wait", Integer.valueOf(tryGetArg(3)));
                getPlayer().sendMessage(successMessage);
                return true;
            } catch (NumberFormatException ex) {
                throw new IllegalStateException(getLocaleString("error.invalid-number", false));
            }
        } else if (tryGetArg(2).equals(getParamsString("set.game-period"))) {
            // game set #game game-period 120
            if (tryGetArg(3).isBlank())
                throw new IllegalStateException(missingSettingValue);
            try {
                gameConfiguration.set("period.game", Integer.valueOf(tryGetArg(3)));
                getPlayer().sendMessage(successMessage);
                return true;
            } catch (NumberFormatException ex) {
                throw new IllegalStateException(getLocaleString("error.invalid-number", false));
            }
        } else if (tryGetArg(2).equals(getParamsString("set.finish-period"))) {
            // game set #game finish-period 120
            if (tryGetArg(3).isBlank())
                throw new IllegalStateException(missingSettingValue);
            try {
                gameConfiguration.set("period.finish", Integer.valueOf(tryGetArg(3)));
                getPlayer().sendMessage(successMessage);
                return true;
            } catch (NumberFormatException ex) {
                throw new IllegalStateException(getLocaleString("error.invalid-number", false));
            }
        }

        throw new IllegalStateException(getLocaleString("error.unsupport-settings"));
    }

}
