package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.block.BottomlessPouchItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BottomlessPouchContainer implements Container {
    public static final int SIZE = 27;

    private final ItemStack pouch;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public BottomlessPouchContainer(ItemStack pouch) {
        this.pouch = pouch;
        load();
    }

    private void load() {
        ListTag list = BottomlessPouchItem.stored(pouch);

        for (int i = 0; i < Math.min(SIZE, list.size()); i++) {
            items.set(i, ItemStack.of(list.getCompound(i)));
        }
    }

    private void save() {
        ListTag list = new ListTag();

        for (int i = 0; i < SIZE; i++) {
            ItemStack stack = items.get(i);

            if (!stack.isEmpty()) {
                CompoundTag tag = new CompoundTag();
                tag.putByte("Slot", (byte) i);
                stack.save(tag);
                list.add(tag);
            }
        }

        pouch.getOrCreateTag().put(BottomlessPouchItem.TAG_ITEMS, list);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = items.get(slot);

        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack split = stack.split(count);

        if (stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }

        setChanged();
        return split;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        save();
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (stack.getItem() instanceof BottomlessPouchItem) {
            return;
        }

        items.set(slot, stack);

        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }

        setChanged();
    }

    @Override
    public void setChanged() {
        save();
    }

    @Override
    public boolean stillValid(Player player) {
        return !pouch.isEmpty() && pouch.getItem() instanceof BottomlessPouchItem;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !(stack.getItem() instanceof BottomlessPouchItem);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < SIZE; i++) {
            items.set(i, ItemStack.EMPTY);
        }

        setChanged();
    }
}
