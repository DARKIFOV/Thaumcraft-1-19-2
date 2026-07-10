package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Stage201 Forge 1.19.2 adapter for original InventoryMob used by ContainerGolem.
 * It exposes the golem's original Inventory NBT-backed slots without inventing a new storage model.
 */
public class GolemInventoryContainer implements Container {
    private final ThaumGolemEntity golem;

    public GolemInventoryContainer(ThaumGolemEntity golem) {
        this.golem = golem;
    }

    public ThaumGolemEntity golem() {
        return golem;
    }

    @Override
    public int getContainerSize() {
        return golem == null ? 0 : golem.activeSlotCount();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return golem == null ? ItemStack.EMPTY : golem.getGolemInventoryStack(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        // Original ContainerGolem exposes SlotGhost / SlotGhostFluid. A generic
        // Container caller must never extract a real stack from these filters.
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (golem == null) {
            return;
        }
        ItemStack copy = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!copy.isEmpty()) {
            copy.setCount(Math.min(copy.getCount(), getMaxStackSize()));
        }
        golem.setGolemInventoryStack(slot, copy);
    }

    @Override
    public void setChanged() {
        if (golem != null) {
            golem.markGolemInventoryChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return golem != null && golem.isAlive() && player.distanceToSqr(golem) < 64.0D;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < getContainerSize(); i++) {
            setItem(i, ItemStack.EMPTY);
        }
    }
}
