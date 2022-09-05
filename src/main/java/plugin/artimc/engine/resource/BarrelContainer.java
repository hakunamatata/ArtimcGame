package plugin.artimc.engine.resource;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import plugin.artimc.utils.BukkitUtil;
import plugin.artimc.utils.StringUtil;

public class BarrelContainer extends ContainerBlock {
    private final Location location;

    public BarrelContainer(String inputString, World world) {
        this(StringUtil.tryGet(0, inputString.split(";")), StringUtil.tryGet(1, inputString.split(";")), StringUtil.tryGet(2, inputString.split(";")), world);
    }

    public BarrelContainer(String strPosition, String stored, String option, World world) {
        super(Material.BARREL, strPosition, stored, option, world);
        location = new Location(world, StringUtil.tryGetDouble(0, strPosition.split(",")), StringUtil.tryGetDouble(1, strPosition.split(",")), StringUtil.tryGetDouble(2, strPosition.split(",")));
        initializeBlock();
        fillBlockData();
    }

    @Override
    protected void onBlockInitialized() {
        super.onBlockInitialized();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    protected void addItem(ItemStack item) {
        BlockState state = getContainer().getState();
        if (state instanceof Barrel) {
            Barrel barrel = (Barrel) state;
            BukkitUtil.addContainerItem(barrel, item);
        }
    }
}
