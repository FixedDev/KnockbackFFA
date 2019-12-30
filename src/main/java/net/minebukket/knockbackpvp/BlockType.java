package net.minebukket.knockbackpvp;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public enum BlockType {


    DEFAULT("Por defecto", Arrays.asList(new ItemStack(Material.STAINED_CLAY, 1, (short) 0),
            new ItemStack(Material.STAINED_CLAY, 1, (short) 4),
            new ItemStack(Material.STAINED_CLAY, 1, (short) 5),
            new ItemStack(Material.STAINED_CLAY, 1, (short) 13),
            new ItemStack(Material.STAINED_CLAY, 1, (short) 2),
            new ItemStack(Material.STAINED_CLAY, 1, (short) 6),
            new ItemStack(Material.STAINED_CLAY, 1, (short) 14)), ""),
    CLAY_RAINBOW("Arcoiris de arcilla", Arrays.asList(new ItemStack(Material.STAINED_CLAY, 1, (short) 10),
            new ItemStack(Material.STAINED_CLAY, 1, (short) 9),
            new ItemStack(Material.STAINED_CLAY, 1, (short) 3),
            new ItemStack(Material.STAINED_CLAY, 1, (short) 11),
            new ItemStack(Material.STAINED_CLAY, 1, (short) 13),
            new ItemStack(Material.STAINED_CLAY, 1, (short) 4),
            new ItemStack(Material.STAINED_CLAY, 1, (short) 14)), "knockbackpvp.blocks.crainbow"),
    GLASS_RAINBOW("Arcoiris de cristal", Arrays.asList(new ItemStack(Material.STAINED_GLASS, 1, (short) 10),
            new ItemStack(Material.STAINED_GLASS, 1, (short) 9),
            new ItemStack(Material.STAINED_GLASS, 1, (short) 3),
            new ItemStack(Material.STAINED_GLASS, 1, (short) 11),
            new ItemStack(Material.STAINED_GLASS, 1, (short) 13),
            new ItemStack(Material.STAINED_GLASS, 1, (short) 4),
            new ItemStack(Material.STAINED_GLASS, 1, (short) 14)), "knockbackpvp.blocks.grainbow"),
    WOOL_RAINBOW("Arcoiris de lana", Arrays.asList(new ItemStack(Material.WOOL, 1, (short) 10),
            new ItemStack(Material.WOOL, 1, (short) 9),
            new ItemStack(Material.WOOL, 1, (short) 3),
            new ItemStack(Material.WOOL, 1, (short) 11),
            new ItemStack(Material.WOOL, 1, (short) 13),
            new ItemStack(Material.WOOL, 1, (short) 4),
            new ItemStack(Material.WOOL, 1, (short) 14)), "knockbackpvp.blocks.wrainbow"),
    BLACK_AND_WHITE("Negro y blanco", Arrays.asList(new ItemStack(Material.WOOL, 1, (short) 15),
            new ItemStack(Material.WOOL, 1, (short) 7),
            new ItemStack(Material.WOOL, 1, (short) 8),
            new ItemStack(Material.WOOL, 1, (short) 0),
            new ItemStack(Material.WOOL, 1, (short) 8),
            new ItemStack(Material.WOOL, 1, (short) 7),
            new ItemStack(Material.WOOL, 1, (short) 15)), "knockbackpvp.blocks.baw");

    @Getter
    private String name;
    @Getter
    private List<ItemStack> blockStates;
    @Getter
    private String blockPermission;

    BlockType(String name, List<ItemStack> blockStates, String blockPermission) {
        this.name = name;
        this.blockStates = blockStates;
        this.blockPermission = blockPermission;
    }
}
