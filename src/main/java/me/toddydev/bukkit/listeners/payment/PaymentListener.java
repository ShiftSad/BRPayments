package me.toddydev.bukkit.listeners.payment;

import de.tr7zw.nbtapi.NBT;
import me.toddydev.bukkit.BukkitMain;
import me.toddydev.bukkit.events.PaymentCompletedEvent;
import me.toddydev.bukkit.events.PaymentExpiredEvent;
import me.toddydev.core.Core;
import me.toddydev.core.api.taskchain.TaskChain;
import me.toddydev.core.cache.Caching;
import me.toddydev.core.model.product.Product;
import me.toddydev.core.model.product.actions.Action;
import me.toddydev.core.model.product.actions.type.ActionType;
import me.toddydev.core.player.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import static me.toddydev.discord.enums.MessageChannel.SELL;
import static org.bukkit.Material.MAP;

public class PaymentListener implements Listener {

    @EventHandler
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        Player player = event.getPlayer();
        User user = Caching.getUserCache().find(player.getUniqueId());
        Product product = Caching.getProductCache().findById(event.getOrder().getProductId());

        Action action = product.getActions().stream().filter(a -> a.getType().equals(ActionType.COLLECT)).findAny().orElse(null);

        title(player, product, action);

        player.sendActionBar(action.getActionBar()
                .replace("&", "§")
                .replace("{player}", player.getName())
                .replace("{displayName}", player.getDisplayName())
                .replace("{product}", product.getName())
        );

        TaskChain.newChain().add(new TaskChain.GenericTask() {
            @Override
            protected void run() {
                for (int slot : player.getInventory().all(Material.MAP).keySet()) {
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (stack == null)continue;
                    if (stack.getType() != MAP)continue;

                    if (NBT.readNbt(stack).getString("brpayments:order") == null) continue;

                    player.getInventory().setItem(slot, user.getItemInHand());
                    user.setItemInHand(null);
                    break;
                }

                product.getRewards().getCommands().forEach(command -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
                });

                product.getRewards().getItems().forEach(item -> {
                    player.getInventory().addItem(item.stack());
                });

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                EmbedBuilder builder = new EmbedBuilder();

                builder.setColor(Color.getColor(BukkitMain.getInstance().getConfig().getString("discord.embeds.sell.color")));
                builder.setTitle(BukkitMain.getInstance().getConfig().getString("discord.embeds.sell.title"));

                builder.setDescription(BukkitMain.getInstance().getConfig().getString("discord.embeds.sell.description")
                        .replace("{player}", player.getName())
                        .replace("{displayName}", player.getDisplayName())
                        .replace("{product}", product.getName())
                );

                builder.addField(new MessageEmbed.Field(BukkitMain.getInstance().getConfig().getString("discord.embeds.sell.fields.player"), player.getName(), true));
                builder.addField(new MessageEmbed.Field(BukkitMain.getInstance().getConfig().getString("discord.embeds.sell.fields.price"), "R$ " + String.format(new Locale("pt", "BR"), "%.2f", product.getPrice()), true));
                builder.addField(new MessageEmbed.Field(BukkitMain.getInstance().getConfig().getString("discord.embeds.sell.fields.rate"), String.format(new Locale("pt", "BR"), "%.2f", ((product.getPrice() * (0.99 / 100)))) + " (0.99%)", false));

                builder.setFooter(BukkitMain.getInstance().getConfig().getString("discord.embeds.sell.footer")
                        .replace("{player}", player.getName())
                        .replace("{displayName}", player.getDisplayName())
                        .replace("{product}", product.getName())
                        .replace("{date}", format.format(System.currentTimeMillis()))
                );

                Core.getDiscord().sendEmbed(SELL, builder);

                user.setTotalPaid(user.getTotalPaid() + product.getPrice());
            }
        }).execute();


    }

    @EventHandler
    public void onPaymentExpired(PaymentExpiredEvent event) {
        Player player = event.getPlayer();
        Product product = Caching.getProductCache().findById(event.getOrder().getProductId());

        Action action = product.getActions().stream().filter(a -> a.getType().equals(ActionType.EXPIRED)).findAny().orElse(null);

        assert action != null;
        title(player, product, action);

        player.sendActionBar(action.getActionBar()
                .replace("&", "§")
                .replace("{player}", player.getName())
                .replace("{displayName}", player.getDisplayName())
                .replace("{product}", product.getName())
        );
    }

    private void title(Player player, Product product, Action action) {
        player.playSound(player.getLocation(), action.getSound(), 5f, 5f);
        player.sendTitlePart(TitlePart.TITLE, PlainTextComponentSerializer.plainText().deserialize(action.getScreen().getTitle().replace("&", "§")));
        player.sendTitlePart(TitlePart.SUBTITLE, PlainTextComponentSerializer.plainText().deserialize(action.getScreen().getSubtitle().replace("&", "§")));
        player.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.of(1, ChronoUnit.SECONDS), Duration.of(2, ChronoUnit.SECONDS), Duration.of(1, ChronoUnit.SECONDS)));

        player.sendMessage(action.getMessage()
                .replace("&", "§")
                .replace("{player}", player.getName())
                .replace("{displayName}", player.getDisplayName())
                .replace("{product}", product.getName())
        );
    }
}
