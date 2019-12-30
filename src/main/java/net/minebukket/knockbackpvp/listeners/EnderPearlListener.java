package net.minebukket.knockbackpvp.listeners;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minebukket.knockbackpvp.InventoryItemType;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import net.minebukket.knockbackpvp.objects.UserSettings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
@AllArgsConstructor
@Getter
public class EnderPearlListener implements Listener {

    private final KnockbackPvpPlugin plugin;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {

        if (event.getEntity().getLastDamageCause() == null) {
            return;
        }

        Player killer = event.getEntity().getKiller();

        if (killer == null) {
            return;
        }

        ListenableFuture<UserSettings> futureUser = plugin.getUserSettingsHandler().getData(killer.getUniqueId());

        Futures.addCallback(futureUser, new FutureCallback<UserSettings>() {
            @Override
            public void onSuccess(UserSettings result) {
                int itemIndex = result.getInventoryItemOrder().entrySet().stream().filter(ent -> ent.getValue() == InventoryItemType.ENDER_PEARL).map(ent -> ent.getKey()).findAny().orElse(-1);

                if (itemIndex == -1) {
                    return;
                }

                killer.getInventory().setItem(itemIndex, new ItemStack(Material.ENDER_PEARL));
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });

    }
}
