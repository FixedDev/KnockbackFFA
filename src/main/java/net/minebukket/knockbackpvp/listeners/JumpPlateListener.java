package net.minebukket.knockbackpvp.listeners;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import net.minebukket.knockbackpvp.InventoryItemType;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import net.minebukket.knockbackpvp.objects.UserSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
public class JumpPlateListener implements Listener {

    private final KnockbackPvpPlugin plugin;
    private Map<UUID, Block> playerJumpPlates;

    public JumpPlateListener(KnockbackPvpPlugin plugin) {
        this.plugin = plugin;
        this.playerJumpPlates = new ConcurrentHashMap<>();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.GOLD_PLATE) return;

        playerJumpPlates.put(event.getPlayer().getUniqueId(), event.getBlockPlaced());

        ListenableFuture<UserSettings> futureUser = plugin.getUserSettingsHandler().getData(event.getPlayer().getUniqueId());

        Futures.addCallback(futureUser, new FutureCallback<UserSettings>() {
            @Override
            public void onSuccess(UserSettings result) {
                if (result == null) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Failed to load your data!");
                    return;
                }

                new BukkitRunnable() {

                    private int secondsRemaining = 10;

                    @Override
                    public void run() {
                        int itemIndex = result.getInventoryItemOrder().entrySet().stream().filter(ent -> ent.getValue() == InventoryItemType.JUMP_PLATE).map(ent -> ent.getKey()).findAny().orElse(-1);

                        if (itemIndex == -1) {
                            Bukkit.getScheduler().cancelTask(getTaskId());
                            return;
                        }

                        Block block = playerJumpPlates.get(event.getPlayer().getUniqueId());

                        if (block == null) {
                            Bukkit.getScheduler().cancelTask(getTaskId());
                            return;
                        }

                        if (secondsRemaining == 0) {
                            block.setType(Material.AIR);

                            ItemStack jumpPlate = new ItemStack(Material.GOLD_PLATE);
                            ItemMeta jumpPlateItemMeta = jumpPlate.getItemMeta();
                            jumpPlateItemMeta.setDisplayName(ChatColor.YELLOW + "Salto");

                            jumpPlate.setItemMeta(jumpPlateItemMeta);

                            event.getPlayer().getInventory().setItem(itemIndex, jumpPlate);

                            Bukkit.getScheduler().cancelTask(getTaskId());
                            return;
                        }

                        ItemStack waitWatch = new ItemStack(Material.WATCH, secondsRemaining);
                        ItemMeta waitWatchItemMeta = waitWatch.getItemMeta();

                        String timeUnit = secondsRemaining == 0 ? "segundos" : secondsRemaining == 1 ? "segundo" : "segundos";

                        waitWatchItemMeta.setDisplayName(ChatColor.YELLOW + "Espera " + secondsRemaining + " " + timeUnit + " para volver a usar esta habilidad!");

                        waitWatch.setItemMeta(waitWatchItemMeta);

                        event.getPlayer().getInventory().setItem(itemIndex, waitWatch);

                        secondsRemaining--;
                    }
                }.runTaskTimer(plugin, 0, 20);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }

        });
    }


    @EventHandler
    public void jumpPlateListener(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (player.getLocation().getBlock().getType() == Material.GOLD_PLATE) {
            Vector playerDirection = player.getLocation().getDirection().multiply(1.5D).setY(1.0D);

            player.setVelocity(playerDirection);

            player.setFallDistance(-9999.0F);
        }
    }

    @EventHandler
    public void jumpPlateItemSpawn(ItemSpawnEvent e){
        if(e.getEntity().getItemStack().getType() == Material.GOLD_PLATE){
            e.getEntity().remove();
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Block block = this.playerJumpPlates
                .remove(event.getPlayer().getUniqueId());
        if (block != null) block.setType(Material.AIR);
    }

    public void cancelCooldown(Player player){
        this.playerJumpPlates.remove(player.getUniqueId());
    }

    public boolean hasCooldown(Player player){
        return this.playerJumpPlates.containsKey(player.getUniqueId());
    }
}

