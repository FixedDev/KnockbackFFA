package net.minebukket.knockbackpvp.listeners;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import net.minebukket.knockbackpvp.objects.UserSettings;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
public class ItemsListener implements Listener {

    private final KnockbackPvpPlugin plugin;

    public ItemsListener(KnockbackPvpPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        loadInventory(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        loadInventory(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryClickEvent event) {
        if (!event.getWhoClicked().hasPermission("knockback.inventory.drag") && event.getWhoClicked().getGameMode() != GameMode.CREATIVE && event.getClickedInventory() != null && event.getClickedInventory().equals(event.getWhoClicked().getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryInteract(InventoryInteractEvent event) {
        if (!event.getWhoClicked().hasPermission("knockback.inventory.drag") && event.getWhoClicked().getGameMode() != GameMode.CREATIVE && event.getInventory().equals(event.getWhoClicked().getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!event.getPlayer().hasPermission("knockback.item.drop") && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onInventoryPickupItem(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() == Material.GOLDEN_APPLE) {

            if (!event.getItem().getMetadata("killedPlayer").isEmpty()) {
                String killedPlayer = event.getItem().getMetadata("killedPlayer").get(0).asString();
                event.getPlayer().sendMessage(ChatColor.YELLOW + String.format("Has sido bendecido con la regeneracion de los dioses por recoger la manzana dorada tirada al morir por %1$s.", killedPlayer));

                event.getItem().remove();

                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 2));

                event.setCancelled(true);
            } else {
                event.getPlayer().sendMessage(ChatColor.YELLOW + String.format("Has sido bendecido con la regeneracion de los dioses por recoger la manzana dorada tirada por ellos."));
            }

            return;
        }

        if (!event.getPlayer().hasPermission("knockback.item.pickup") && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().clear();

 /*       EntityDamageEvent lastDamage = listener.getEntity().getLastDamageCause();

        if (lastDamage != null && lastDamage.getCause() != EntityDamageEvent.DamageCause.VOID) {
            Location deadLocation = listener.getEntity().getLocation();

            Item droppedItem = deadLocation.getWorld().dropItem(deadLocation.add(0, 1, 0), new ItemStack(Material.GOLDEN_APPLE));

            droppedItem.setMetadata("killedPlayer", new FixedMetadataValue(plugin, listener.getEntity().getName()));
            return;
        }*/

        Player killer = event.getEntity().getKiller();

        if (killer == null) {
            return;
        }

        killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 2));
        killer.sendMessage(ChatColor.YELLOW + String.format("Has sido bendecido por la regeneracion de los dioses por matar a %1$s.", event.getEntity().getName()));

    }

    public void loadInventory(final Player player) {
        player.getInventory().clear();

        ListenableFuture<UserSettings> futureUserSettings = plugin.getUserSettingsHandler().getData(player.getUniqueId());

        Futures.addCallback(futureUserSettings, new FutureCallback<UserSettings>() {
            @Override
            public void onSuccess(UserSettings result) {
                if (result == null) {
                    player.sendMessage(ChatColor.RED + "Failed to load your user settings, rejoin to resolve this!");
                    return;
                }

                result.getInventoryItemOrder().forEach((itemIndex, itemType) -> {
                    switch (itemType) {
                        case KNOCKBACK_STICK:
                            ItemStack knockbackStick = new ItemStack(Material.STICK);
                            ItemMeta itemMeta = knockbackStick.getItemMeta();

                            itemMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
                            itemMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);

                            knockbackStick.setItemMeta(itemMeta);

                            player.getInventory().setItem(itemIndex, knockbackStick);
                            break;
                        case BLOCKS:
                            ItemStack item = result.getUserBlockType().getBlockStates().get(0).clone();
                            item.setAmount((short) 64);
                            player.getInventory().setItem(itemIndex, item);
                            break;
                        case JUMP_PLATE:
                            ItemStack jumpPlate = new ItemStack(Material.GOLD_PLATE);
                            ItemMeta jumpPlateItemMeta = jumpPlate.getItemMeta();

                            jumpPlateItemMeta.setDisplayName(ChatColor.YELLOW + "Salto");

                            jumpPlate.setItemMeta(jumpPlateItemMeta);

                            player.getInventory().setItem(itemIndex, jumpPlate);
                            break;
                        case BOW:
                            ItemStack bow = new ItemStack(Material.BOW);
                            player.getInventory().setItem(9, new ItemStack(Material.ARROW));
                            player.getInventory().setItem(itemIndex, bow);
                            break;
                        case ENDER_PEARL:
                            ItemStack enderPearl = new ItemStack(Material.ENDER_PEARL);
                            player.getInventory().setItem(itemIndex, enderPearl);
                            break;
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }


}
