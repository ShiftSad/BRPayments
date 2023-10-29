package me.toddydev.bukkit.task;

import de.tr7zw.nbtapi.NBT;
import me.toddydev.bukkit.BukkitMain;
import me.toddydev.bukkit.events.PaymentCompletedEvent;
import me.toddydev.bukkit.events.PaymentExpiredEvent;
import me.toddydev.core.cache.Caching;
import me.toddydev.core.database.tables.Tables;
import me.toddydev.core.model.order.Order;
import me.toddydev.core.model.order.status.OrderStatus;
import me.toddydev.core.services.Services;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

import static me.toddydev.core.model.order.status.OrderStatus.PAID;
import static org.bukkit.Material.MAP;

public class PayTask extends BukkitRunnable {
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getMetadata("brpayments:order").isEmpty()) {
                continue;
            }

            MetadataValue value = player.getMetadata("brpayments:order").stream().filter(metadataValue -> metadataValue.getOwningPlugin().equals(BukkitMain.getInstance())).findFirst().orElse(null);

            if (value == null) {
                continue;
            }

            long time = value.asLong();
            if (time > System.currentTimeMillis()) {
                continue;
            }

            Order order = Caching.getOrdersCache().findByPayer(player.getUniqueId());
            player.removeMetadata("brpayments:order", BukkitMain.getInstance());

            OrderStatus status = Services.getMercadoPagoService().check(order);
            order.setStatus(status);

            if (status != PAID) {
                order.setStatus(OrderStatus.EXPIRED);
            }

            Tables.getOrders().update(order);

            Arrays.stream(player.getInventory().getContents())
                    .filter(item ->
                            item != null &&
                            item.getType() == MAP &&
                            NBT.readNbt(item).getString("brpayments:order") != null
                    ).forEach(item -> player.getInventory().remove(item));

            if (status == PAID) Bukkit.getPluginManager().callEvent(new PaymentCompletedEvent(player, order));
            else Bukkit.getPluginManager().callEvent(new PaymentExpiredEvent(player, order));

            Caching.getOrdersCache().remove(order);
        }
    }
}
