package plugin.artimc.engine.resource;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.ItemStack;
import plugin.artimc.utils.StringUtil;
import plugin.artimc.utils.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * 地图中的实体资源
 * 例如：箱子、生物等
 */
public abstract class ContainerBlock {
    private BlockFace face;
    private World world;
    private Block container;
    private final Material material;
    private final String strPosition;
    private final String stored;
    private final String option;

    public ContainerBlock(Material material, String strPosition, String stored, String option, World world) {
        this.container = null;
        this.world = world;
        this.material = material;
        this.strPosition = strPosition;
        this.stored = stored;
        this.option = option;
    }

    public void setFace(String facing) {
        if (facing != null || !facing.isBlank()) {
            try {
                face = BlockFace.valueOf(facing.toUpperCase());

            } catch (Exception e) {
                e.printStackTrace();
                face = BlockFace.NORTH;
            }

            try {
                BlockData blockData = getContainer().getBlockData();
                Directional directional = (Directional) blockData;
                directional.setFacing(face);
                getContainer().setBlockData(blockData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public World getWorld() {
        return world;
    }

    protected Block getContainer() {
        return container;
    }

    protected Material getMaterial() {
        return material;
    }

    protected String getStored() {
        return stored;
    }

    protected String getOption() {
        return option;
    }

    protected void setContainer(Block container) {
        this.container = container;
    }

    abstract Location getLocation();

    protected void onBlockInitialized() {

    }

    protected void initializeBlock() {
        getWorld().setBlockData(getLocation(), getMaterial().createBlockData());
        setContainer(getWorld().getBlockAt(getLocation()));
        onBlockInitialized();
        Bukkit.getLogger().info(String.format("在 world: %s %s,%s,%s 放置了箱子", getWorld().getName(), getLocation().getBlockX(), getLocation().getBlockY(), getLocation().getBlockZ()));
    }

    protected void fillBlockData() {
        boolean randomize = getOption().toLowerCase().contains("random");
        if (randomize) {
            randomFill();
        } else {
            strictFill();
        }

    }

    protected abstract void addItem(ItemStack item);

    private void strictFill() {
        for (String s : getStored().split(",")) {
            Material material = StringUtil.tryGetMaterial(0, s.split(":"));
            int amount = StringUtil.tryGetInt(1, s.split(":"));
            addItem(new ItemStack(material, amount));
        }
    }

    private void randomFill() {
        int max = Integer.parseInt(getOption().toLowerCase().replace("random", "0"));
        if (max < 1) max = 1;
        List<String> itemsStr = new java.util.ArrayList<>(Arrays.stream(getStored().split(",")).toList());
        for (int i = 0; i < max; i++) {
            String item = Utils.getRandomElement(itemsStr);
            Material material = StringUtil.tryGetMaterial(0, item.split(":"));
            int amount = StringUtil.tryGetInt(1, item.split(":"));
            addItem(new ItemStack(material, amount));
            itemsStr.remove(item);
        }
    }

}
