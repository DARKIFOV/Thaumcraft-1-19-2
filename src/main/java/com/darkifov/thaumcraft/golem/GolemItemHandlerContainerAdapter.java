package com.darkifov.thaumcraft.golem;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Bridges Forge item capabilities into the legacy Container-oriented golem AI.
 * The adapter never exposes a mutable handler stack: mutations are committed
 * through extractItem/insertItem, preserving automation rules of modded blocks.
 */
public final class GolemItemHandlerContainerAdapter implements Container {
    private final IItemHandler handler;

    public GolemItemHandlerContainerAdapter(IItemHandler handler) {
        this.handler = handler;
    }

    public IItemHandler handler() {
        return handler;
    }

    @Override
    public int getContainerSize() {
        return handler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (!handler.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= handler.getSlots()) {
            return ItemStack.EMPTY;
        }
        return handler.getStackInSlot(slot).copy();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= handler.getSlots() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        return handler.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= handler.getSlots()) {
            return ItemStack.EMPTY;
        }
        return handler.extractItem(slot, handler.getStackInSlot(slot).getCount(), false);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= handler.getSlots()) {
            return;
        }
        ItemStack safe = stack == null ? ItemStack.EMPTY : stack.copy();
        if (handler instanceof IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(slot, safe);
            return;
        }
        ItemStack current = handler.getStackInSlot(slot);
        if (safe.isEmpty()) {
            handler.extractItem(slot, current.getCount(), false);
        } else if (current.isEmpty()) {
            handler.insertItem(slot, safe, false);
        }
    }

    @Override
    public void setChanged() {
        // Capability implementations own their dirty/sync lifecycle.
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                handler.extractItem(slot, stack.getCount(), false);
            }
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= handler.getSlots() || stack == null || stack.isEmpty()) {
            return false;
        }
        return handler.insertItem(slot, stack.copy(), true).getCount() < stack.getCount();
    }

    public boolean canExtract(int slot, int amount) {
        return slot >= 0 && slot < handler.getSlots() && !handler.extractItem(slot, amount, true).isEmpty();
    }

    public ItemStack insert(ItemStack stack, boolean simulate) {
        ItemStack working = stack == null ? ItemStack.EMPTY : stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !working.isEmpty(); slot++) {
            working = handler.insertItem(slot, working, simulate);
        }
        return working;
    }

    public ItemStack insertIntoSlots(ItemStack stack, Iterable<Integer> slots, boolean simulate) {
        ItemStack working = stack == null ? ItemStack.EMPTY : stack.copy();
        for (int slot : slots) {
            if (slot >= 0 && slot < handler.getSlots() && !working.isEmpty()) {
                working = handler.insertItem(slot, working, simulate);
            }
        }
        return working;
    }
}
