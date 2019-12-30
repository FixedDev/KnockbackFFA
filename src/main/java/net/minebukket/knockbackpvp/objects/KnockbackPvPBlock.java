package net.minebukket.knockbackpvp.objects;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.minebukket.util.ArrayBackedIterator;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
@EqualsAndHashCode
@Getter
public class KnockbackPvPBlock {

    private Block block;
    @Getter(value = AccessLevel.NONE)
    private ArrayBackedIterator<ItemStack> nextStates;

    @Setter
    private int runCount = 0;

    private KnockbackPvPBlock(Block block, ArrayBackedIterator<ItemStack> nextStates) {
        this.block = block;
        this.nextStates = nextStates;
    }

    public ItemStack getNextState() {
        return nextStates.next();
    }

    public boolean hasNextState() {
        return nextStates.hasNext();
    }

    public static class Factory {
        @Getter
        private static final Factory instance = new Factory();

        private Factory() {
        }

        public KnockbackPvPBlock newBlock(Block block, ItemStack... nextStates) {
            return this.newBlock0(block, nextStates);
        }

        public KnockbackPvPBlock newBlock(Block block, List<ItemStack> nextStates) {
            return this.newBlock0(block, nextStates.toArray(new ItemStack[nextStates.size()]));
        }

        public KnockbackPvPBlock newBlock(Block block, Iterator<ItemStack> iterator) {
            List<ItemStack> nextStates = new ArrayList<>();
            iterator.forEachRemaining(nextStates::add);

            return this.newBlock(block, nextStates);
        }

        private KnockbackPvPBlock newBlock0(Block block, ItemStack[] nextStates) {
            return new KnockbackPvPBlock(block, new ArrayBackedIterator<>(nextStates));
        }
    }


}



