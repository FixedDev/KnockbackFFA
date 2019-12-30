package net.minebukket.knockbackpvp.listeners;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import net.minebukket.knockbackpvp.objects.UserData;
import net.minebukket.knockbackpvp.objects.UserSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
public class UserListener implements Listener {

    private KnockbackPvpPlugin plugin;

    public UserListener(KnockbackPvpPlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        Futures.addCallback(plugin.getUserDataHandler().getData(event.getUniqueId()), new FutureCallback<UserData>() {
            @Override
            public void onSuccess(UserData result) {
                if(result == null){
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Failed the load of your settings, report this to a developer!");
                    plugin.getLogger().severe("Failed to load user data of " + event.getUniqueId());
                    return;
                }


                Player player = Bukkit.getPlayer(result.getUniqueId());

                if (player != null) {
                    player.sendMessage(ChatColor.GREEN + "Sucessfully loaded your data.");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Failed the load of your data, report this to a developer!");
                plugin.getLogger().severe("Failed to load user data of " + event.getUniqueId() + " exception: ");
                t.printStackTrace();
            }
        });

        Futures.addCallback(plugin.getUserSettingsHandler().getData(event.getUniqueId()), new FutureCallback<UserSettings>() {
            @Override
            public void onSuccess(UserSettings result) {
                if(result == null){
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Failed the load of your settings, report this to a developer!");
                    plugin.getLogger().severe("Failed to load user settings of " + event.getUniqueId());
                    return;
                }

                Player player = Bukkit.getPlayer(result.getUniqueId());

                if (player != null) {
                    player.sendMessage(ChatColor.GREEN + "Sucessfully loaded your settings.");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Failed the load of your settings, report this to a developer!");
                plugin.getLogger().severe("Failed to load user settings of " + event.getUniqueId() + " exception: ");
                t.printStackTrace();
            }
        });
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getUserDataHandler().saveAndUnloadData(event.getPlayer().getUniqueId());
        plugin.getUserSettingsHandler().saveAndUnloadData(event.getPlayer().getUniqueId());
    }
}
