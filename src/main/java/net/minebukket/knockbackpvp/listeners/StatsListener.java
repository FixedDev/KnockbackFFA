package net.minebukket.knockbackpvp.listeners;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minebukket.knockbackpvp.handlers.abstractmanager.DataHandler;
import net.minebukket.knockbackpvp.objects.UserData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
@AllArgsConstructor
@Getter
public class StatsListener implements Listener {

    private final DataHandler<UUID, UserData> userDataManager;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getEntity();

        ListenableFuture<UserData> futureKilled = userDataManager.getData(killed.getUniqueId());

        Futures.addCallback(futureKilled, new FutureCallback<UserData>() {
            @Override
            public void onSuccess(UserData result) {
                result.setUserDeaths(result.getUserDeaths() + 1);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });


        Player killer = event.getEntity().getKiller();

        if (killer == null) {
            return;
        }

        ListenableFuture<UserData> futureKiller = userDataManager.getData(killer.getUniqueId());
        Futures.addCallback(futureKiller, new FutureCallback<UserData>() {
            @Override
            public void onSuccess(UserData result) {
                result.setUserKills(result.getUserKills() + 1);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });


    }
}
