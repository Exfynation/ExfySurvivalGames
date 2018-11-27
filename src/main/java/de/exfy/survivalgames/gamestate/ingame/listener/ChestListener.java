package de.exfy.survivalgames.gamestate.ingame.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Stream;

public class ChestListener implements Listener {

    private Map<Material, Double> items;
    private List<Location> usedInventories;

    public ChestListener() {
        usedInventories = new ArrayList<>();
        items = new HashMap<>();
        items.put(Material.APPLE, 5D);
        items.put(Material.GOLDEN_APPLE, 0.8D);
        items.put(Material.WOOD_SWORD, 4D);
        items.put(Material.STONE_AXE, 2.5D);
        items.put(Material.STONE_SWORD, 2D);
        items.put(Material.IRON_SWORD, 1D);
        items.put(Material.WEB, 4D);
        items.put(Material.BOW, 2D);
        items.put(Material.ARROW, 1.6D);
        items.put(Material.BREAD, 5D);
        items.put(Material.STICK, 2D);
        items.put(Material.DIAMOND, 0.4D);
        items.put(Material.IRON_INGOT, 1.45D);
        items.put(Material.IRON_HELMET, 1.35D);
        items.put(Material.IRON_CHESTPLATE, 1.35D);
        items.put(Material.IRON_LEGGINGS, 1.35D);
        items.put(Material.IRON_BOOTS, 1.35D);
        items.put(Material.LEATHER_HELMET, 2.35D);
        items.put(Material.LEATHER_CHESTPLATE, 2.35D);
        items.put(Material.LEATHER_LEGGINGS, 2.35D);
        items.put(Material.LEATHER_BOOTS, 2.35D);
        items.put(Material.EXP_BOTTLE, 3D);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getInventory().getHolder() instanceof Chest){
            Chest chest = (Chest) e.getInventory().getHolder();

            if(usedInventories.contains(chest.getBlock().getLocation())) return;
            usedInventories.add(chest.getBlock().getLocation());

            Inventory inv = chest.getBlockInventory();

            Random random = new Random();

            for(int i=0; i < 27; i++) {
                boolean setItem = (Math.random()*10+1) > 9.5;
                if(!setItem) continue;

                Material material = getRandomMaterial(items.entrySet().stream(), random);
                ItemStack stack = new ItemStack(material);
                if(material == Material.ARROW || material == Material.APPLE || material == material.WEB || material == material.BREAD || material == material.STICK || material == Material.EXP_BOTTLE) {
                    stack.setAmount((int)Math.random()*14+2);
                }

                if(material == Material.DIAMOND || material == material.IRON_INGOT) {
                    stack.setAmount((int)Math.random()*3+1);
                }
                inv.setItem(i, stack);
            }
        }
    }

    public static <E> E getRandomMaterial(Stream<Map.Entry<E, Double>> weights, Random random) {
        return weights
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), -Math.log(random.nextDouble()) / e.getValue()))
                .min(Comparator.comparing(AbstractMap.SimpleEntry::getValue))
                .orElseThrow(IllegalArgumentException::new).getKey();
    }
}
