package com.darkifov.thaumcraft.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

/**
 * Side-aware inventory bridge used by TC4 machines.
 * It prefers Forge ITEM_HANDLER capabilities, then falls back to vanilla
 * Container/WorldlyContainer semantics. Simulation never mutates the target.
 */
public final class TC4ItemTransferRuntime {
    private TC4ItemTransferRuntime() {
    }

    public static boolean hasInventory(Level level, BlockPos pos, Direction side) {
        if (level == null || pos == null) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side).isPresent()
                || blockEntity instanceof Container;
    }

    public static boolean canInsert(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return stack != null && !stack.isEmpty() && insert(level, pos, side, stack, true).isEmpty();
    }

    public static ItemStack insert(Level level, BlockPos pos, Direction side, ItemStack stack, boolean simulate) {
        if (level == null || pos == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack.copy();
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return stack.copy();
        }

        IItemHandler handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
        if (handler != null) {
            ItemStack remainder = stack.copy();
            for (int slot = 0; slot < handler.getSlots() && !remainder.isEmpty(); slot++) {
                remainder = handler.insertItem(slot, remainder, simulate);
            }
            return remainder;
        }

        if (blockEntity instanceof Container container) {
            return insertContainer(container, side, stack, simulate);
        }
        return stack.copy();
    }

    private static ItemStack insertContainer(Container container, Direction side, ItemStack stack, boolean simulate) {
        ItemStack remainder = stack.copy();
        int[] slots;
        if (container instanceof WorldlyContainer worldly && side != null) {
            slots = worldly.getSlotsForFace(side);
        } else {
            slots = new int[container.getContainerSize()];
            for (int i = 0; i < slots.length; i++) {
                slots[i] = i;
            }
        }

        for (int slot : slots) {
            if (remainder.isEmpty() || slot < 0 || slot >= container.getContainerSize()) {
                continue;
            }
            if (!container.canPlaceItem(slot, remainder)) {
                continue;
            }
            if (container instanceof WorldlyContainer worldly && side != null
                    && !worldly.canPlaceItemThroughFace(slot, remainder, side)) {
                continue;
            }

            ItemStack existing = container.getItem(slot);
            int max = Math.min(container.getMaxStackSize(), remainder.getMaxStackSize());
            if (existing.isEmpty()) {
                int moved = Math.min(max, remainder.getCount());
                if (!simulate) {
                    ItemStack placed = remainder.copy();
                    placed.setCount(moved);
                    container.setItem(slot, placed);
                    container.setChanged();
                }
                remainder.shrink(moved);
                continue;
            }
            if (!ItemStack.isSameItemSameTags(existing, remainder)) {
                continue;
            }
            int room = Math.min(max, existing.getMaxStackSize()) - existing.getCount();
            if (room <= 0) {
                continue;
            }
            int moved = Math.min(room, remainder.getCount());
            if (!simulate) {
                ItemStack merged = existing.copy();
                merged.grow(moved);
                container.setItem(slot, merged);
                container.setChanged();
            }
            remainder.shrink(moved);
        }
        return remainder;
    }
}
