package plugin.artimc.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class PvPItemControl {
    private PvPGame game;
    private Map<String, Boolean> banMaterials;
    private Map<String, String> matMap;
    private Map<UUID, Integer> playerElytraDamages;

    public PvPItemControl(PvPGame game) {
        this.game = game;
        initialbanMaterials();
        initialMaterialNameMap();
        playerElytraDamages = new HashMap<>();
    }

    private void initialbanMaterials() {
        banMaterials = new HashMap<>();
        banMaterials.put("enchanted_golden_apple", true);
        banMaterials.put("golden_apple", true);
        banMaterials.put("elytra", false);
        banMaterials.put("ender_pearl", false);
        banMaterials.put("totem_of_undying", true);
        banMaterials.put("lava_bucket", false);
        banMaterials.put("wither_skeleton_skull", true);
        banMaterials.put("carved_pumpkin", true);
        banMaterials.put("iron_block", true);
        banMaterials.put("iron_ingot", false);
        banMaterials.put("wolf_spawn_egg", true);
        banMaterials.put("ender_chest", true);
        banMaterials.put("anvil", false);
        banMaterials.put("chipped_anvil", false);
        banMaterials.put("damaged_anvil", false);
        banMaterials.put("crafting_table", true);
        banMaterials.put("tnt", true);
    }

    public List<String> getItemStatus() {
        List<String> items = new ArrayList<>();
        banMaterials.forEach((k, s) -> {
            items.add((s ? "§7" : "§a") + getAlterName(k));
        });
        return items;
    }

    private void initialMaterialNameMap() {
        matMap = new HashMap<>();
        matMap.put("enchanted_golden_apple", "附魔金苹果");
        matMap.put("附魔金苹果", "enchanted_golden_apple");

        matMap.put("golden_apple", "金苹果");
        matMap.put("金苹果", "golden_apple");

        matMap.put("elytra", "鞘翅");
        matMap.put("鞘翅", "elytra");

        matMap.put("ender_pearl", "末影珍珠");
        matMap.put("末影珍珠", "ender_pearl");

        matMap.put("totem_of_undying", "不死图腾");
        matMap.put("不死图腾", "totem_of_undying");

        matMap.put("lava_bucket", "熔岩桶");
        matMap.put("熔岩桶", "lava_bucket");

        matMap.put("wither_skeleton_skull", "凋灵骷髅头颅");
        matMap.put("凋灵骷髅头颅", "wither_skeleton_skull");

        matMap.put("carved_pumpkin", "雕刻过的南瓜");
        matMap.put("雕刻过的南瓜", "carved_pumpkin");

        matMap.put("iron_block", "铁块");
        matMap.put("铁块", "iron_block");

        matMap.put("iron_ingot", "铁锭");
        matMap.put("铁锭", "iron_ingot");

        matMap.put("wolf_spawn_egg", "狼刷怪蛋");
        matMap.put("狼刷怪蛋", "wolf_spawn_egg");

        matMap.put("ender_chest", "末影箱");
        matMap.put("末影箱", "ender_chest");

        matMap.put("anvil", "铁砧");
        matMap.put("铁砧", "anvil");

        matMap.put("chipped_anvil", "开裂的铁砧");
        matMap.put("开裂的铁砧", "chipped_anvil");

        matMap.put("damaged_anvil", "损坏的铁砧");
        matMap.put("损坏的铁砧", "damaged_anvil");

        matMap.put("crafting_table", "工作台");
        matMap.put("工作台", "crafting_table");

        matMap.put("tnt", "tnt");

        matMap.put("enchanted_golden_apple", "附魔金苹果");
        matMap.put("附魔金苹果", "enchanted_golden_apple");

    }

    private String getAlterName(String name) {
        if (matMap.containsKey(name.toLowerCase())) {
            return matMap.get(name.toLowerCase());
        }
        return name;
    }

    public boolean ban(String itemName) {
        String material = getAlterName(itemName);
        if (banMaterials.containsKey(material)) {
            banMaterials.put(material, true);
            return true;
        }
        return false;
    }

    public boolean unban(String itemName) {
        String material = getAlterName(itemName);
        if (banMaterials.containsKey(material)) {
            banMaterials.put(material, false);
            return true;
        }
        return false;
    }

    public void replaceItems() {
        if (game.getHostParty() != null) {
            for (Player p : game.getHostParty().getOnlinePlayers()) {
                replaceItemForPlayer(p);
            }
        }

        if (game.getGuestParty() != null) {
            for (Player p : game.getGuestParty().getOnlinePlayers()) {
                replaceItemForPlayer(p);
            }
        }
    }


    public void recoverItems() {
        if (game.getHostParty() != null) {
            for (Player p : game.getHostParty().getOnlinePlayers()) {
                recoverItemForPlayer(p);
            }
        }

        if (game.getGuestParty() != null) {
            for (Player p : game.getGuestParty().getOnlinePlayers()) {
                recoverItemForPlayer(p);
            }
        }
    }

    private void replaceItemForPlayer(Player p) {
        for (ItemStack stack : p.getInventory().getContents()) {
            if (stack == null || stack.getType().isAir())
                continue;
            if (Boolean.TRUE.equals(banMaterials.getOrDefault(stack.getType().name().toLowerCase(), false))) {
                ItemMeta i = stack.getItemMeta();
                if (stack.getType() == Material.ELYTRA) {
                    Damageable d = (Damageable) i;
                    if (!playerElytraDamages.containsKey(p.getUniqueId()) || d.getDamage() > 0) {
                        playerElytraDamages.put(p.getUniqueId(), d.getDamage());
                        // 鞘翅耐久度变为0
                        d.setDamage(999999);
                        stack.setItemMeta(i);
                    }
                } else {
                    i.setDisplayName(replaceDisplayName(stack.getType()));
                    stack.setType(Material.BARRIER);
                    stack.setItemMeta(i);
                }
            }
        }
    }

    public void recoverItemForPlayer(Player p) {
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack stack = p.getInventory().getItem(i);
            if (stack == null || (stack.getType() != Material.BARRIER && stack.getType() != Material.ELYTRA))
                continue;

            ItemMeta m = stack.getItemMeta();
            if (stack.getType() == Material.ELYTRA) {
                Damageable d = (Damageable) m;
                if (playerElytraDamages.containsKey(p.getUniqueId())) {
                    d.setDamage(playerElytraDamages.get(p.getUniqueId()));
                }
                stack.setItemMeta(m);
            } else {
                String matName = matMap.getOrDefault(recoverDisplayName(m), "AIR");
                Material material;
                try {
                    material = Material.valueOf(matName.toUpperCase());
                } catch (Exception ex) {
                    material = Material.AIR;
                }
                p.getInventory().setItem(i, new ItemStack(material, stack.getAmount()));
            }
        }
    }

    private String replaceDisplayName(Material material) {
        String matName = material.name().toLowerCase();
        return "§c被禁用的 " + matMap.get(matName);
    }

    private String recoverDisplayName(ItemMeta meta) {
        String replacedName = meta.getDisplayName().replace("§c被禁用的 ", "");
        return replacedName;
    }
}
