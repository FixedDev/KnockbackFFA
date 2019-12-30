package net.minebukket.knockbackpvp.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import lombok.Getter;
import net.minebukket.knockbackpvp.BlockType;
import net.minebukket.knockbackpvp.InventoryItemType;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import net.minebukket.knockbackpvp.objects.UserSettings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
public class BlocksCommand {

    private static SmartInventory inventory;

    @Command(names = {"blocks,bloques"}, max = 0, onlyPlayer = true)
    public static boolean blocksCommand(Player player, CommandContext context) {
        getInventory().open(player);
        return true;
    }

    private static SmartInventory getInventory() {
        if (inventory == null) {
            inventory = SmartInventory.builder()
                    .id("blocksGUI")
                    .title(ChatColor.YELLOW + "Bloques")
                    .size(3, 9)
                    .provider(BlocksInventoryProvider.getInstance())
                    .build();
        }
        return inventory;
    }

    private static void changeBlock(Player player, BlockType type) {
        ListenableFuture<UserSettings> futureUserSettings = KnockbackPvpPlugin.getPlugin().getUserSettingsHandler().getData(player.getUniqueId());
        Futures.addCallback(futureUserSettings, new FutureCallback<UserSettings>() {
            @Override
            public void onSuccess(UserSettings userSettings) {
                userSettings.setUserBlockType(type);
                player.sendMessage(ChatColor.YELLOW + String.format("Has cambiado tu tipo de bloque a %1$s.", type.getName()));

                int blocksIndex = userSettings.getInventoryItemOrder().entrySet().stream().filter(ent -> ent.getValue() == InventoryItemType.BLOCKS).map(ent -> ent.getKey()).findFirst().orElse(-1);

                if(blocksIndex == -1){
                    return;
                }

                player.getInventory().getItem(blocksIndex).setType(type.getBlockStates().get(0).getType());
                player.getInventory().getItem(blocksIndex).setTypeId(type.getBlockStates().get(0).getDurability());

            }


            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

    }

    private static class BlocksInventoryProvider implements InventoryProvider {

        @Getter
        private static BlocksInventoryProvider instance = new BlocksInventoryProvider();

        public BlocksInventoryProvider() {
            if (instance != null) {
                throw new IllegalStateException("This is a singleton, you understand?!?!?!");
            }
        }

        @Override
        public void init(Player player, InventoryContents inventoryContents) {
            SlotIterator iterator = inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0);
            Pagination pagination = inventoryContents.pagination();

            List<ClickableItem> itemList = new ArrayList<>();

            for (BlockType type : BlockType.values()) {
                ItemStack itemStack = type.getBlockStates().get(0).clone();
                ItemMeta itemMeta = itemStack.getItemMeta();

                itemMeta.setDisplayName(type.getName());

                String permission = player.hasPermission(type.getBlockPermission()) ? "" : ChatColor.RED + ChatColor.BOLD.toString() + "No tienes permiso para usar este bloque!";
                itemMeta.setLore(permission.isEmpty() ? Collections.emptyList() : ImmutableList.of(permission));

                itemStack.setItemMeta(itemMeta);

                itemList.add(ClickableItem.of(itemStack, e -> {
                    changeBlock(player, type);
                }));
            }

            pagination.setItems(itemList.toArray(new ClickableItem[itemList.size()]));

            if (BlockType.values().length > 27) {
                pagination.setItemsPerPage(17);
            } else {
                pagination.setItemsPerPage(27);
            }

            pagination.addToIterator(iterator);

            if (!pagination.isLast()) {
                ItemStack nextPage = new ItemStack(Material.ARROW);

                ItemMeta itemMeta = nextPage.getItemMeta();
                itemMeta.setDisplayName(ChatColor.YELLOW + "Siguiente pagina");

                nextPage.setItemMeta(itemMeta);

                inventoryContents.set(2, 5, ClickableItem.of(nextPage,
                        e -> getInventory().open(player, pagination.next().getPage())));
            }

            if (!pagination.isFirst()) {
                ItemStack previousPage = new ItemStack(Material.ARROW);

                ItemMeta itemMeta = previousPage.getItemMeta();
                itemMeta.setDisplayName(ChatColor.YELLOW + "Anterior pagina");

                previousPage.setItemMeta(itemMeta);

                inventoryContents.set(2, 3, ClickableItem.of(previousPage,
                        e -> getInventory().open(player, pagination.next().getPage())));
            }

        }

        @Override
        public void update(Player player, InventoryContents inventoryContents) {
     /*     int state = inventoryContents.property("state", 0);
            inventoryContents.setProperty("state", state + 1);

            if (state % 5 == 0) {
                return;
            }

            init(player, inventoryContents);*/
        }
    }
}
