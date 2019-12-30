package net.minebukket.knockbackpvp.listeners;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import net.minebukket.knockbackpvp.maps.GameMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;
import java.util.Objects;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
@Getter
@AllArgsConstructor
public class MapsListener implements Listener {

    private final KnockbackPvpPlugin plugin;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        List<GameMap> gameMaps = plugin.getMapLoader().getAllGameMaps();

        GameMap currentMap = null;

        if (gameMaps.size() == 1) {
            currentMap = gameMaps.stream().filter(Objects::nonNull).findFirst().orElse(null);

        } else if (gameMaps.size() > 1) {
            currentMap = plugin.getMapManager().getCurrentMap();
        }

        event.setRespawnLocation(currentMap.getMapSpawnLocation());
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        List<GameMap> gameMaps = plugin.getMapLoader().getAllGameMaps();

        GameMap currentMap;

        if (gameMaps.size() == 1) {
            currentMap = gameMaps.stream().filter(Objects::nonNull).findFirst().orElse(null);
        } else if(gameMaps.size() > 1){
            currentMap = plugin.getMapManager().getCurrentMap();
        } else {
            return;
        }

        event.getPlayer().teleport(currentMap.getMapSpawnLocation());

        if(gameMaps.size() == 1){
            return;
        }

        if(plugin.getVoteMapManager().isMapVotating()){
            event.getPlayer().sendMessage(plugin.getVoteMapManager().getMapsVoteInformation());
        }

        event.getPlayer().sendMessage(plugin.getMapManager().getCurrentMap().getMapInformation());
    }
}
