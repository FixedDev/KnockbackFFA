package net.minebukket.knockbackpvp.maps;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.Data;
import net.minebukket.knockbackpvp.exceptions.MapLoadException;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
@Data
public class GameMap {
    private final String name;
    private final String mapDisplayName;
    private final World world;
    private final Location mapSpawnLocation;
    private final List<String> mapBuilders;

    private int highLimit;
    private int lowLimit;

    private File mapDataFile;

    public GameMap(String mapName, String mapDisplayName, World world, Location mapSpawnLocation, List<String> mapBuilders) {
        Preconditions.checkArgument(StringUtils.isBlank(mapName), "Map name can't be empty");
        Preconditions.checkArgument(StringUtils.isBlank(mapDisplayName), "Map display name can't be empty");
        Preconditions.checkNotNull(world, "World can't be null!");
        Preconditions.checkNotNull(mapSpawnLocation, "Map spawn location can't be null!");
        Preconditions.checkNotNull(mapBuilders, "Map builders can't be null!");
        this.name = mapName;
        this.mapDisplayName = mapDisplayName;
        this.world = world;
        this.mapSpawnLocation = mapSpawnLocation;
        this.mapBuilders = ImmutableList.copyOf(mapBuilders);
    }

    public GameMap(File file) throws MapLoadException {
        mapDataFile = file;

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            doc.getDocumentElement().normalize();

            // A xml map file must have only 1 map
            Node mapNode = doc.getElementsByTagName("gameMap").item(0);
            if (mapNode == null || mapNode.getNodeType() != Node.ELEMENT_NODE) {
                throw new MapLoadException("Invalid map file, map node doesn't exist or is invalid");
            }

            Element nodeElement = (Element) mapNode;

            mapNameAndDisplayNameLoad:
            {
                name = nodeElement.getElementsByTagName("mapName").item(0).getTextContent();

                NodeList displayNameElement = nodeElement.getElementsByTagName("displayMapName");

                if (displayNameElement == null || displayNameElement.getLength() == 0) {
                    mapDisplayName = name;
                    break mapNameAndDisplayNameLoad;
                }

                String nullableDisplayName = displayNameElement.item(0).getTextContent();
                mapDisplayName = StringUtils.isNotBlank(ChatColor.translateAlternateColorCodes('&', nullableDisplayName)) ? nullableDisplayName : name;
            }


            worldLoading:
            {
                Node worldNode = nodeElement.getElementsByTagName("world").item(0);

                if (worldNode == null || worldNode.getNodeType() != Node.ELEMENT_NODE) {
                    throw new MapLoadException("Invalid map file, world node doesn't exist or is invalid");
                }

                Element worldElement = (Element) worldNode;

                lowLimit = Integer.parseInt(worldElement.getAttribute("lowLimit"));
                highLimit = Integer.parseInt(worldElement.getAttribute("highLimit"));

                world = Bukkit.createWorld(WorldCreator.name(worldElement.getAttribute("name")));

                world.setGameRuleValue("doDaylightCycle", "false");

                Node spawnLocationNode = nodeElement.getElementsByTagName("spawnLocation").item(0);
                if (spawnLocationNode == null || spawnLocationNode.getNodeType() != Node.ELEMENT_NODE) {
                    throw new MapLoadException("Invalid map file, spawn location node doesn't exist");
                }

                Element spawnLocationElement = (Element) spawnLocationNode;

                double x, y, z;

                try {
                    x = Double.parseDouble(spawnLocationElement.getElementsByTagName("x").item(0).getTextContent());
                    y = Double.parseDouble(spawnLocationElement.getElementsByTagName("y").item(0).getTextContent());
                    z = Double.parseDouble(spawnLocationElement.getElementsByTagName("z").item(0).getTextContent());
                } catch (NumberFormatException e) {
                    throw new MapLoadException("Failed to parse spawn location", e);
                }

                mapSpawnLocation = new Location(world, x, y, z);

            }


            mapBuildersLoad:
            {
                NodeList mapBuildersElement = nodeElement.getElementsByTagName("mapBuilders");

                if (mapBuildersElement == null || mapBuildersElement.getLength() == 0) {
                    mapBuilders = ImmutableList.of();
                    break mapBuildersLoad;
                }

                NodeList mapBuildersList = mapBuildersElement.item(0).getChildNodes();

                ImmutableList.Builder<String> immutableListBuilder = ImmutableList.builder();

                for (int i = 0; i < mapBuildersList.getLength(); i++) {
                    Node mapBuilderNode = mapBuildersList.item(i);

                    immutableListBuilder.add(mapBuilderNode.getTextContent());
                }

                mapBuilders = immutableListBuilder.build();
            }

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new MapLoadException(ex);
        }


    }

    public String[] getMapInformation() {
        List<String> mapInformation = new ArrayList<>();

        mapInformation.add(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + Strings.repeat("-", 24));
        mapInformation.add(ChatColor.YELLOW + "Mapa" + ChatColor.DARK_GRAY + ":" + ChatColor.GOLD + " " + ChatColor.translateAlternateColorCodes('&', mapDisplayName));

        if (mapBuilders != null || !mapBuilders.isEmpty()) {
            mapInformation.add(ChatColor.YELLOW + "Constructores" + ChatColor.DARK_GRAY + ":");

            mapBuilders.stream().filter(Objects::nonNull).filter(builder -> !builder.trim().isEmpty()).filter(builder -> !"\n".equalsIgnoreCase(builder)).forEach(builder -> mapInformation.add("  " + ChatColor.GOLD + builder));
        }

        mapInformation.add(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + Strings.repeat("-", 24));

        return mapInformation.toArray(new String[mapInformation.size()]);
    }
}
