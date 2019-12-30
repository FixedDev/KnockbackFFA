package net.minebukket.knockbackpvp.maps;

import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import net.minebukket.knockbackpvp.exceptions.MapLoadException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
public class GameMapLoader {

    private final File mapsFolder;
    private KnockbackPvpPlugin plugin;

    private Map<String, GameMap> gameMaps;

    private ReentrantLock lock;

    public GameMapLoader(KnockbackPvpPlugin plugin) {
        lock = new ReentrantLock();

        this.plugin = plugin;
        mapsFolder = new File(plugin.getDataFolder(), "maps");

        if (!mapsFolder.exists()) mapsFolder.mkdir();

        plugin.saveResource("maps/ExampleMap.xml", true);

        gameMaps = new ConcurrentHashMap<>();

        this.reloadMaps();
    }


    public void reloadMaps() {
        lock.lock();

        try {
            for (File mapFile : mapsFolder.listFiles()) {
                try {
                    if (mapFile.getName().contains("ExampleMap")) {
                        continue;
                    }

                    GameMap gameMap = new GameMap(mapFile);

                    if (gameMap.getName().equalsIgnoreCase("example")) {
                        continue;
                    }

                    gameMaps.put(gameMap.getName(), gameMap);

                    plugin.getLogger().log(Level.INFO, "Loaded {0} map", gameMap.getName());
                } catch (MapLoadException e) {
                    plugin.getLogger().severe("Failed to load map " + mapFile.getName() + " reason: " + e.getMessage());
                }
            }
        } finally {
            lock.unlock();
        }
    }


    public Optional<GameMap> getGameMap(String name) {
        lock.lock();
        try {
            GameMap gameMap = gameMaps.get(name);
            return gameMap == null ? Optional.empty() : Optional.of(gameMap);
        } finally {
            lock.unlock();
        }
    }

    public List<GameMap> getAllGameMaps() {
        lock.lock();
        try {
            return new ArrayList<>(this.gameMaps.values());
        } finally {
            lock.unlock();
        }
    }
}
