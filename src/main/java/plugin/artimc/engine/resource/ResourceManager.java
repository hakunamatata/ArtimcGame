package plugin.artimc.engine.resource;

import plugin.artimc.engine.IGame;
import plugin.artimc.utils.StringUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resource Manager
 * 管理地图中的资源
 */
public class ResourceManager {
    private final IGame game;
    private final Set<ContainerBlock> containers;

    public ResourceManager(IGame game) {
        this.game = game;
        this.containers = new HashSet<>();
    }

    public void addGameContainer() {
        List<String> containerResources = game.getMap().getConfig().getStringList("resources.blocks");
        for (String input : containerResources)
            addContainer(input);
    }

    public void addContainer(String inputString) {
        if (inputString == null || inputString.isBlank()) return;
        String[] form = inputString.split(" ")[0].split(":");
        String material = StringUtil.tryGet(0, form);
        String facing = StringUtil.tryGet(1, form);
        ContainerBlock container;
        if ("barrel".equalsIgnoreCase(material)) {
            container = new BarrelContainer(inputString.split(" ")[1], game.getGameWorld().getWorld());
        } else {
            container = new ChestContainer(inputString.split(" ")[1], game.getGameWorld().getWorld());
        }
        container.setFace(facing);
        containers.add(container);

    }
}
