package net.minebukket.knockbackpvp.listeners;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import net.minebukket.knockbackpvp.InventoryItemType;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import net.minebukket.knockbackpvp.objects.UserSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;


/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
public class BowListener implements Listener {

    private final KnockbackPvpPlugin plugin;

    public BowListener(KnockbackPvpPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player shooter = (Player) event.getEntity();

        if (shooter.getGameMode() == GameMode.CREATIVE) return;

        ListenableFuture<UserSettings> futureUser = plugin.getUserSettingsHandler().getData(shooter.getUniqueId());
        Futures.addCallback(futureUser, new FutureCallback<UserSettings>() {
            @Override
            public void onSuccess(UserSettings result) {
                if (result == null) {
                    shooter.sendMessage(ChatColor.RED + "Failed to load your data!");
                    return;
                }

                new BukkitRunnable() {

                    private int secondsRemaining = 10;

                    @Override
                    public void run() {

                        if (shooter == null || shooter.isDead()) {
                            Bukkit.getScheduler().cancelTask(getTaskId());
                            return;
                        }

                        int itemIndex = result.getInventoryItemOrder().entrySet().stream().filter(ent -> ent.getValue() == InventoryItemType.BOW).map(ent -> ent.getKey()).findAny().orElse(-1);

                        if (itemIndex == -1) {
                            Bukkit.getScheduler().cancelTask(getTaskId());
                            return;
                        }


                        ItemStack bow = shooter.getInventory().getItem(itemIndex);

                        if (bow == null) {
                            Bukkit.getScheduler().cancelTask(getTaskId());
                            return;
                        }

                        ItemMeta bowItemMeta = bow.getItemMeta();

                        if (secondsRemaining <= 0) {
                            shooter.getInventory().setItem(9, new ItemStack(Material.ARROW));

                            if (bowItemMeta != null) {

                                bowItemMeta.setDisplayName("");

                                bow.setItemMeta(bowItemMeta);
                            }

                            Bukkit.getScheduler().cancelTask(getTaskId());
                            return;
                        }

                        if (bowItemMeta != null) {
                            String timeUnit = secondsRemaining == 0 ? "segundos" : secondsRemaining == 1 ? "segundo" : "segundos";

                            bowItemMeta.setDisplayName(ChatColor.YELLOW + "Espera " + secondsRemaining + " " + timeUnit + " para volver a usar esta habilidad!");

                            bow.setItemMeta(bowItemMeta);

                        }

                        secondsRemaining--;
                        return;
                    }
                }.runTaskTimer(plugin, 0, 20);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }

        });
    }

    @EventHandler(ignoreCancelled = true)
    public void arrowHit(ProjectileHitEvent event) {
        if (event.getEntityType() != EntityType.ARROW) return;

        event.getEntity().remove();
    }

}

