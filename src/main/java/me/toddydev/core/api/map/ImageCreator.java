package me.toddydev.core.api.map;

import de.tr7zw.nbtapi.NBT;
import me.toddydev.bukkit.BukkitMain;
import me.toddydev.core.api.placeholder.PlaceholderLoader;
import me.toddydev.core.cache.Caching;
import me.toddydev.core.database.tables.Tables;
import me.toddydev.core.utils.item.ItemBuilder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ImageCreator {

    static final private MiniMessage mm = MiniMessage.miniMessage();

    public static void generateMap(String data, Player player) {
        MapView view = Bukkit.createMap(player.getWorld());
        view.setTrackingPosition(false);
        view.getRenderers().clear();

        QRCodeRenderer render = new QRCodeRenderer(data);
        view.addRenderer(render);


            List<String> lore = PlaceholderLoader.setPlaceholders(player, BukkitMain.getMessagesConfig().getStringList("item-qrcode-description"));
            lore.replaceAll(line -> line.replace("&", "§"));
            ItemStack item = new ItemBuilder(Material.MAP, view.getId())
                    .name(PlaceholderLoader.setPlaceholders(player, BukkitMain.getMessagesConfig().getString("item-qrcode-name").replace("&", "§")))
                    .lore(lore)
                    .build();

        NBT.modify(item, nbt -> {
            nbt.setString("brpayments:order", "");
        });

        NBT.modify(player, nbt -> {
            nbt.setString("brpayments:order", String.valueOf(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)));
        });

        var user = Caching.getUserCache().find(player.getUniqueId());
        var order = Caching.getOrdersCache().findByPayer(player.getUniqueId());

        var component = mm.deserialize(PlaceholderLoader.setPlaceholders(player, BukkitMain.getMessagesConfig().getString("success-payment-link")
                .replace("&","§")
                .replace("{ticket_link}", order.getTicketLink())
        ));
        component = component.hoverEvent(HoverEvent.showText(mm.deserialize(PlaceholderLoader.setPlaceholders(player, BukkitMain.getMessagesConfig().getString("success-payment-link-hover")
                .replace("&", "§")
                .replace("{nl}", "\n"
        )))));
        component = component.clickEvent(ClickEvent.openUrl(order.getTicketLink()));

        player.sendMessage(component);
        var mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() != Material.AIR) {
            user.setItemInHand(mainHand);
        }

        user.setTotalOrders(user.getTotalOrders() + 1);
        Tables.getUsers().update(user);

        player.getInventory().setItemInMainHand(item);
    }
}