package me.toddydev.core.utils.item;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.util.List;
import java.util.UUID;


public class ItemBuilder {

    private final ItemStack stack;
    private final ItemMeta meta;


    public ItemBuilder(Material material, int id) {
        stack = new ItemStack(material, 1, (short) id);
        meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
    }

    public ItemBuilder(Material material) {
        stack = new ItemStack(material, 1);
        meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
    }

    public ItemBuilder amount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder lore(String... lore) {
        meta.setLore(java.util.Arrays.asList(lore));
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder setMapView(MapView view) {
        MapMeta meta = (MapMeta) stack.getItemMeta();
        meta.setMapView(view);
        stack.setItemMeta(meta);
        return this;
    }

//    public ItemBuilder skullOwner(String owner) {
//        ((org.bukkit.inventory.meta.SkullMeta) meta).setOwner(owner);
//        return this;
//    }

    public ItemBuilder texture(String code) {
        NBT.modify(stack, nbt -> {
            final ReadWriteNBT skullOwnerCompound = nbt.getOrCreateCompound("SkullOwner");

            skullOwnerCompound.setUUID("Id", UUID.randomUUID());

            skullOwnerCompound.getOrCreateCompound("Properties")
                    .getCompoundList("textures")
                    .addCompound()
                    .setString("Value", code);
        });

        return this;
    }

    public ItemBuilder texture(Player player) {
        NBT.modify(stack, nbt -> {
            final ReadWriteNBT skullOwnerCompound = nbt.getOrCreateCompound("SkullOwner");

            skullOwnerCompound.setUUID("Id", player.getUniqueId());

            skullOwnerCompound.getOrCreateCompound("Properties")
                    .getCompoundList("textures")
                    .addCompound()
                    .setString("Value", player.getUniqueId().toString());
        });

        return this;
    }

    public ItemBuilder data(short data) {
        stack.setDurability(data);
        return this;
    }

    public ItemStack build() {
        stack.setItemMeta(meta);
        return stack;
    }
}
