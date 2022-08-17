package plugin.artimc.commands.executor.admin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.item.GameItem;

import java.util.List;

public class TestCommand extends DefaultCommand {
    public TestCommand(CommandContext context) {
        super(context);
    }

    @Override
    public List<String> suggest() {
        return List.of();
    }

    @Override
    public boolean execute() {
//        if (getGame() != null) {
//            Location playerLocation = getPlayer().getLocation();
//            playerLocation.setX(playerLocation.getX() - 10);
//            getGame().dropItem(new ItemStack(Material.EMERALD, 64), playerLocation);
//        }
        return super.execute();
    }
}
