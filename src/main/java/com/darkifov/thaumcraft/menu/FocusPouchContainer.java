package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.block.FocusPouchItem;
import com.darkifov.thaumcraft.block.WandFocusItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Stage186: 1.19.2 adapter for original InventoryFocusPouch.
 * Original tokens preserved: ItemStack[] stackList = new ItemStack[18]; getInventoryStackLimit() == 1; getMaxStackSize() == 1;
 * canPlaceItem only accepts ItemFocusBasic, mapped here to WandFocusItem.
 */
public class FocusPouchContainer implements Container {
    public static final int SIZE = 18;
    private final ItemStack pouch;
    private final NonNullList<ItemStack> stackList = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public FocusPouchContainer(ItemStack pouch) {
        this.pouch = pouch;
        load();
    }

    private void load() {
        ItemStack[] original = FocusPouchItem.getInventory(pouch);
        for (int i = 0; i < Math.min(SIZE, original.length); i++) {
            stackList.set(i, original[i].isEmpty() ? ItemStack.EMPTY : original[i].copy());
        }
    }

    private void save() {
        ItemStack[] out = new ItemStack[SIZE];
        for (int i = 0; i < SIZE; i++) {
            out[i] = stackList.get(i).isEmpty() ? ItemStack.EMPTY : stackList.get(i).copy();
        }
        FocusPouchItem.setInventory(pouch, out);
    }

    public static ItemStack[] readInventoryList(ItemStack item) {
        ItemStack[] stackList = new ItemStack[SIZE];
        for (int i = 0; i < SIZE; i++) {
            stackList[i] = ItemStack.EMPTY;
        }
        if (item.hasTag()) {
            ListTag var2 = item.getOrCreateTag().getList(FocusPouchItem.TAG_INVENTORY, 10);
            for (int var3 = 0; var3 < var2.size(); var3++) {
                CompoundTag var4 = var2.getCompound(var3);
                int var5 = var4.getByte("Slot") & 255;
                if (var5 >= 0 && var5 < stackList.length) {
                    stackList[var5] = ItemStack.of(var4);
                }
            }
        }
        return stackList;
    }

    public static void writeInventoryList(ItemStack item, ItemStack[] stackList) {
        ListTag var2 = new ListTag();
        for (int var3 = 0; var3 < stackList.length && var3 < SIZE; var3++) {
            ItemStack stack = stackList[var3];
            if (stack != null && !stack.isEmpty()) {
                CompoundTag var4 = new CompoundTag();
                var4.putByte("Slot", (byte)var3);
                ItemStack copy = stack.copy();
                copy.setCount(1);
                copy.save(var4);
                var2.add(var4);
            }
        }
        item.getOrCreateTag().put(FocusPouchItem.TAG_INVENTORY, var2);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : stackList) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SIZE ? stackList.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = getItem(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.split(Math.min(count, 1));
        if (stack.isEmpty()) {
            stackList.set(slot, ItemStack.EMPTY);
        }
        setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SIZE) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stackList.get(slot);
        stackList.set(slot, ItemStack.EMPTY);
        save();
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SIZE) {
            return;
        }
        if (!stack.isEmpty() && !(stack.getItem() instanceof WandFocusItem)) {
            return;
        }
        ItemStack copy = stack.copy();
        if (!copy.isEmpty()) {
            copy.setCount(1);
        }
        stackList.set(slot, copy);
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
        save();
    }

    @Override
    public boolean stillValid(Player player) {
        return !pouch.isEmpty() && pouch.getItem() instanceof FocusPouchItem;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof WandFocusItem;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < SIZE; i++) {
            stackList.set(i, ItemStack.EMPTY);
        }
        setChanged();
    }
}
