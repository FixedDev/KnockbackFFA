package net.minebukket.knockbackpvp.maps;

import lombok.Getter;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
public class GameMapManager implements Listener {

    @Getter
    private final KnockbackPvpPlugin plugin;
    @Getter
    private final GameMapLoader gameMapLoader;
    @Getter
    private final long secondsBeforeChangeMap;
    @Getter
    private final long secondsBeforeStartVoting;


    private BukkitTask changeMapTask;
    private Date nextMapChange;

    private GameMap currentMap;

    public GameMapManager(KnockbackPvpPlugin plugin, GameMapLoader gameMapLoader, long minutesBeforeChangeMap, long secondsBeforeStartVoting) {
        this.plugin = plugin;
        this.gameMapLoader = gameMapLoader;
        this.secondsBeforeChangeMap = minutesBeforeChangeMap;
        this.secondsBeforeStartVoting = secondsBeforeStartVoting;

        this.enableMapManager();
    }

    private void enableMapManager() {
        nextMapChange = Date.from(Instant.now().plus(secondsBeforeChangeMap, ChronoUnit.SECONDS));

        List<GameMap> gameMaps = new ArrayList<>(gameMapLoader.getAllGameMaps());

        Collections.shuffle(gameMaps);

        currentMap = gameMaps.stream().filter(Objects::nonNull).findAny().orElse(null);

        changeMapTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (nextMapChange.before(new Date())) {
                    plugin.getVoteMapManager().setMapVotating(false);

                    currentMap = plugin.getVoteMapManager().getNextMap(true);
                    plugin.getVoteMapManager().cleanAll();

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.teleport(currentMap.getMapSpawnLocation());
                        plugin.getItemsListener().loadInventory(player);
                        player.sendMessage(getCurrentMap().getMapInformation());

                    });

                    nextMapChange = Date.from(Instant.now().plus(secondsBeforeChangeMap, ChronoUnit.SECONDS));
                }

                //     long betweenNowAndNextChange = nextMapChange.toInstant().until(Instant.now(), ChronoUnit.SECONDS);
                long betweenNowAndNextChange = Instant.now().until(nextMapChange.toInstant(), ChronoUnit.SECONDS);

                if (!plugin.getVoteMapManager().isMapVotating() && betweenNowAndNextChange <= secondsBeforeStartVoting ) {
                    plugin.getVoteMapManager().setMapVotating(true);
                }

                if (betweenNowAndNextChange <= 10) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "El mapa cambiara en " + betweenNowAndNextChange + " segundos.");
                }

            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }
}
