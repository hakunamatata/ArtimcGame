package plugin.artimc.engine.item;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import plugin.artimc.engine.Game;
import plugin.artimc.engine.IGameListener;
import plugin.artimc.engine.event.GameItemPickupEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏中可被捡起的物品
 * 自定义物品，用ArmorStand作为原型
 * 可触发自定义捡起物品的事件
 */
public class GameItem implements Listener, AutoCloseable {

    Location location;

    public ItemStack getItemStack() {
        return itemStack;
    }

    ItemStack itemStack;
    Game game;
    Hologram hologram;
    IGameListener listener;
    Map<String, Object> meta;
    boolean hasPickup;

    public GameItem(ItemStack item, Game game) {
        meta = new HashMap<>();
        this.itemStack = item;
        this.game = game;
        game.getPlugin().getServer().getPluginManager().registerEvents(this, game.getPlugin());
        this.hasPickup = false;
    }

    public void setItemMeta(String key, Object value) {
        meta.put(key, value);
    }

    public Object getItemMeta(String key) {
        return meta.get(key);
    }


    /**
     * 使用 Holographic API
     *
     * @param location
     */
    public GameItem drop(Location location) {
        this.location = location;
        hologram = HologramsAPI.createHologram(game.getPlugin(), location);
        hologram.appendItemLine(itemStack);
        this.listener = game;
        return this;
    }

    public void pickUp(Player player) {
        hologram.delete();
        hasPickup = true;
        listener = null;
        player.getInventory().addItem(itemStack);
    }


    /**
     * 当玩家靠近掉落物时，触发物品被捡起事件
     *
     * @param event
     */
    @EventHandler
    void onPickup(PlayerMoveEvent event) {

        try {
            // 玩家不在当前游戏中，不处理
            if (!game.contains(event.getPlayer())) return;
            // 玩家所在世界与当前游戏所在世界不同，不处理
            if (!event.getPlayer().getWorld().equals(game.getGameMap().getWorld())) return;
            // 数据为准备，不处理
            if (listener == null || hasPickup || hologram == null || itemStack == null || location == null) return;
            // 距离在规定的范围内，视为捡起物品
            if (event.getPlayer().getLocation().distance(location) <= game.getPlugin().getConfig().getInt("settings.pickup-distance", 2)) {
                // fire pick up event
                GameItemPickupEvent pickupEvent = new GameItemPickupEvent(this, event.getPlayer(), location, game);
                listener.onGameItemPickup(pickupEvent);
            }

        } catch (Exception ex) {
            game.getPlugin().getLogger().warning("onGameItemPickup Error: " + ex.getMessage());
        }
    }

    @Override
    public void close() throws Exception {

    }
}
