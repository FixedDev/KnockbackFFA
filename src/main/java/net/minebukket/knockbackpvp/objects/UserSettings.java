package net.minebukket.knockbackpvp.objects;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minebukket.knockbackpvp.BlockType;
import net.minebukket.knockbackpvp.InventoryItemType;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minebukket.knockbackpvp.InventoryItemType.*;

@Entity
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class UserSettings {

    @Transient
    public static final ImmutableMap<Integer, InventoryItemType> defaultItemOrder = new ImmutableMap.Builder<Integer, InventoryItemType>().put(0, KNOCKBACK_STICK).put(1, BLOCKS).put(4, JUMP_PLATE).put(7, ENDER_PEARL).put(8, BOW).build();

    @Id
    private final UUID uniqueId;

    private BlockType userBlockType = BlockType.DEFAULT;

    private Map<Integer, InventoryItemType> inventoryItemOrder = new HashMap<>(defaultItemOrder);


    private UserSettings() {
        this.uniqueId = null;
    }
}
