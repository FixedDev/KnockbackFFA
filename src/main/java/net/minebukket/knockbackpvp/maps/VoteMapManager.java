package net.minebukket.knockbackpvp.maps;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
@RequiredArgsConstructor
public class VoteMapManager {

    private final KnockbackPvpPlugin plugin;
    private final GameMapManager gameMapManager;

    private Map<GameMap, Integer> mapVotes = new HashMap<GameMap, Integer>() {
        @Override
        public Integer get(Object key) {
            Integer result = super.get(key);
            return result == null ? 0 : result;
        }
    };

    private List<UUID> alreadyVotedPlayers = new ArrayList<>();

    private List<GameMap> availableMaps;

    @Getter
    private boolean mapVotating;

    private ReentrantLock lock = new ReentrantLock();

    /**
     * This method cleans the votated maps, the players that already voted and the available maps
     */
    public void cleanAll() {
        lock.lock();
        try {
            availableMaps.clear();
            alreadyVotedPlayers.clear();
            mapVotes.clear();
        } finally {
            lock.unlock();
        }
    }

    public List<GameMap> getAvailableMapsForVote() {
        lock.lock();
        try {
            if (availableMaps != null) {
                return availableMaps;
            }

            List<GameMap> availableMaps = new ArrayList<>();

            Iterator<GameMap> mapsIterator = gameMapManager.getGameMapLoader().getAllGameMaps().iterator();
            while (mapsIterator.hasNext() && availableMaps.size() <= 4) {
                GameMap map = mapsIterator.next();

                if (map.equals(gameMapManager.getCurrentMap())) {
                    continue;
                }

                availableMaps.add(map);
            }

            this.availableMaps = availableMaps;

            return availableMaps;
        } finally {
            lock.unlock();
        }

    }

    public void voteForMap(Player player, int mapNumber) {
        List<GameMap> gameMaps = getAvailableMapsForVote();
        Preconditions.checkArgument(gameMaps.size() >= mapNumber && mapNumber > 0, "Voted map number is higher than available maps or is 0");

        voteForMap(player, gameMaps.get(mapNumber - 1));
    }


    public void voteForMap(int mapNumber) {
        List<GameMap> gameMaps = getAvailableMapsForVote();
        Preconditions.checkArgument(gameMaps.size() >= mapNumber && mapNumber > 0, "Voted map number is higher than available maps or is 0");

        voteForMap(gameMaps.get(mapNumber - 1));
    }

    public void voteForMap(Player player, GameMap map) {
        lock.lock();

        try {
            if (alreadyVotedPlayers == null) {
                alreadyVotedPlayers = new ArrayList<>();
            }

            if(alreadyVotedPlayers.contains(player.getUniqueId())){
                throw new IllegalStateException("The player " + player.getUniqueId() + " already voted for a map");
            }
        } finally {
            this.lock.unlock();
        }

        this.voteForMap(map);
    }

    public void voteForMap(GameMap map) {
        lock.lock();
        try {
            mapVotes.put(map, mapVotes.get(map) + 1);
        } finally {
            lock.unlock();
        }
    }

    public GameMap getNextMap() {
        return this.getNextMap(false);
    }

    public GameMap getNextMap(boolean broadcastIfNoVotes) {
        lock.lock();
        try {
            if (mapVotes.isEmpty()) {
                List<GameMap> gameMapList = new ArrayList<>(gameMapManager.getGameMapLoader().getAllGameMaps());

                gameMapList = gameMapList.stream().filter(gameMap -> !gameMap.equals(gameMapManager.getCurrentMap())).collect(Collectors.toList());

                Collections.shuffle(gameMapList);

                if (broadcastIfNoVotes) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "No hubo votos hacia ningun mapa, un mapa al azar fue seleccionado");
                }

                return gameMapList.get(0);
            }

            Integer maxVotesReached = Collections.max(mapVotes.values());
            List<Map.Entry<GameMap, Integer>> mapsWithHighestVotes = mapVotes.entrySet()
                    .parallelStream()
                    .filter(entry -> entry != null && entry.getKey() != null && entry.getValue() != null)
                    .filter(gameMapIntegerEntry -> gameMapIntegerEntry.getValue() == maxVotesReached)
                    .collect(Collectors.toList());

            Collections.shuffle(mapsWithHighestVotes);


            return mapsWithHighestVotes.get(0).getKey();
        } finally {
            lock.unlock();
        }
    }

    public void setMapVotating(boolean mapVotating) {
        if (this.mapVotating == mapVotating) {
            return;
        }

        this.mapVotating = mapVotating;

        if (mapVotating) {
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(getMapsVoteInformation()));
        }
    }

    public String[] getMapsVoteInformation() {
        List<String> voteInformation = new ArrayList<>();

        voteInformation.add(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + Strings.repeat("-", 24));

        voteInformation.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Iniciada la votacion de mapas!");
        voteInformation.add(ChatColor.YELLOW + "Puedes votar por" + ChatColor.DARK_GRAY + ": ");

        int index = 1;

        for (GameMap gameMap : getAvailableMapsForVote()) {
            voteInformation.add("  " + ChatColor.YELLOW + index + ") " + ChatColor.GOLD + ChatColor.translateAlternateColorCodes('&', gameMap.getMapDisplayName()));
            index++;
        }

        voteInformation.add(ChatColor.YELLOW + "Usa /vote <numero> para votar por tu mapa favorito!");
        voteInformation.add(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + Strings.repeat("-", 24));

        return voteInformation.toArray(new String[voteInformation.size()]);
    }

}
