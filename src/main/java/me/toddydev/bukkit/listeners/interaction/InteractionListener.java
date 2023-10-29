package me.toddydev.bukkit.listeners.interaction;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Material.AIR;
import static org.bukkit.Material.MAP;

public class InteractionListener implements Listener {

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        if (item.getType() != MAP) return;
        if (NBT.readNbt(item).getString("brpayments:order") == null) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item == null)return;
        if (item.getType() != MAP) return;

        NBT.readNbt(item).getString("brpayments:order");

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == AIR)return;

        if (item.getType() != MAP)return;

        NBT.readNbt(item).getString("brpayments:order");

        event.setCancelled(true);
    }
}
