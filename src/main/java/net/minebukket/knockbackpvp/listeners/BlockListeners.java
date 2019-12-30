package net.minebukket.knockbackpvp.listeners;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import net.minebukket.knockbackpvp.objects.KnockbackPvPBlock;
import net.minebukket.knockbackpvp.objects.UserSettings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;


/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
public class BlockListeners implements Listener {

    private final KnockbackPvpPlugin plugin;
    private Multimap<UUID, KnockbackPvPBlock> playerBlocks;

    private int runCountBeforeUpdate;

    public BlockListeners(KnockbackPvpPlugin plugin, int runCountBeforeUpdate) {
        this.plugin = plugin;
        this.playerBlocks = ArrayListMultimap.create();

        this.runCountBeforeUpdate = runCountBeforeUpdate;

        plugin.getServer().getScheduler().runTaskTimer(plugin, new UpdateBlockRunnable(), 0, 1L);
    }

    public boolean isBlockPlacedBy(Player player, Block block) {
        return this.playerBlocks.containsEntry(player.getUniqueId(), block);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().hasPermission("knockback.block.break") && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        if(event.isCancelled()){
            return;
        }

        ListenableFuture<UserSettings> futureUserSettings = plugin.getUserSettingsHandler().getData(event.getPlayer().getUniqueId());

        Futures.addCallback(futureUserSettings, new FutureCallback<UserSettings>() {
            @Override
            public void onSuccess(UserSettings result) {
                if (result == null) {
                    plugin.getLogger().severe("Failed to load user settings of " + event.getPlayer().getUniqueId());
                    return;
                }

                ItemStack firstState = result.getUserBlockType().getBlockStates().get(0);

                Block block = event.getBlockPlaced();

                if (block.getType() != firstState.getType() || block.getState().getRawData() != firstState.getDurability())
                    return;

                KnockbackPvPBlock.Factory blockFactory = KnockbackPvPBlock.Factory.getInstance();

                playerBlocks.put(event.getPlayer().getUniqueId(), blockFactory.newBlock(event.getBlockPlaced(), result.getUserBlockType().getBlockStates()));

            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        if (this.playerBlocks.containsKey(e.getPlayer().getUniqueId())) {
            this.removePlayerBlocks(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent e) {
        if (this.playerBlocks.containsKey(e.getEntity().getUniqueId())) {
            this.removePlayerBlocks(e.getEntity().getUniqueId());
        }
    }

    public void removePlayerBlocks(Player player) {
        ListenableFuture<UserSettings> futureUserSettings = plugin.getUserSettingsHandler().getData(player.getUniqueId());

        Futures.addCallback(futureUserSettings, new FutureCallback<UserSettings>() {
            @Override
            public void onSuccess(UserSettings result) {
                ItemStack itemStack = result.getUserBlockType().getBlockStates().stream().filter(Objects::nonNull).findFirst().orElse(null);

                playerBlocks.asMap().get(player.getUniqueId()).forEach((block) -> {
                    block.getBlock().setType(Material.AIR);
                    player.getInventory().addItem(itemStack);
                });

                playerBlocks.removeAll(player.getUniqueId());
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });


    }

    public void removePlayerBlocks(UUID playerUUID) {
        playerBlocks.asMap().get(playerUUID).forEach((block) -> {
            block.getBlock().setType(Material.AIR);
        });

        this.playerBlocks.removeAll(playerUUID);
    }

    public class UpdateBlockRunnable implements Runnable {


        @Override
        public void run() {
            playerBlocks.asMap().forEach((uuid, blocks) -> {

                Iterator<KnockbackPvPBlock> blockIterator = blocks.iterator();

                while (blockIterator.hasNext()) {
                    KnockbackPvPBlock block = blockIterator.next();

                    if (block.getRunCount() % runCountBeforeUpdate > 0) {
                        block.setRunCount(block.getRunCount() + 1);
                        continue;
                    }

                    if (!block.hasNextState()) {
                        block.getBlock().setType(Material.AIR);
                        playerBlocks.remove(uuid, block);

                        blockIterator = blocks.iterator();

                        ListenableFuture<UserSettings> futureUserSettings = plugin.getUserSettingsHandler().getData(uuid);

                        Futures.addCallback(futureUserSettings, new FutureCallback<UserSettings>() {
                            @Override
                            public void onSuccess(UserSettings result) {
                                Player targetPlayer = Bukkit.getPlayer(result.getUniqueId());
                                if (targetPlayer != null && targetPlayer.getGameMode() != GameMode.CREATIVE) {
                                    targetPlayer.getInventory().addItem(result.getUserBlockType().getBlockStates().get(0));
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    } else {
                        ItemStack nextState = block.getNextState();

                        BlockState state = block.getBlock().getState();
                        state.setType(nextState.getType());
                        state.setRawData(((byte) nextState.getDurability()));
                        state.update();
                    }
                    block.setRunCount(block.getRunCount() + 1);

                }

            });
        }
    }

}

