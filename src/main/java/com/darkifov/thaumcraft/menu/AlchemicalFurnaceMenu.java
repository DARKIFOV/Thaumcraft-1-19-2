package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AlchemicalFurnaceMenu extends AbstractContainerMenu {
    private final AlchemicalFurnaceBlockEntity furnace;
    private final ContainerData data;
    private final BlockPos pos;

    public AlchemicalFurnaceMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, resolve(inventory, buffer.readBlockPos()), new SimpleContainerData(5));
    }

    public AlchemicalFurnaceMenu(int id, Inventory inventory, AlchemicalFurnaceBlockEntity furnace, ContainerData data) {
        super(ThaumcraftMod.ALCHEMICAL_FURNACE_MENU.get(), id);
        this.furnace = furnace;
        this.data = data;
        this.pos = furnace.getBlockPos();
        checkContainerSize(furnace, 2);
        checkContainerDataCount(data, 5);
        addDataSlots(data);

        addSlot(new Slot(furnace, AlchemicalFurnaceBlockEntity.SLOT_INPUT, 80, 8) {
            @Override public boolean mayPlace(ItemStack stack) {
                return furnace.canPlaceItem(AlchemicalFurnaceBlockEntity.SLOT_INPUT, stack);
            }
        });
        addSlot(new Slot(furnace, AlchemicalFurnaceBlockEntity.SLOT_FUEL, 80, 48) {
            @Override public boolean mayPlace(ItemStack stack) {
                return furnace.canPlaceItem(AlchemicalFurnaceBlockEntity.SLOT_FUEL, stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    private static AlchemicalFurnaceBlockEntity resolve(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level.getBlockEntity(pos);
        if (blockEntity instanceof AlchemicalFurnaceBlockEntity furnace) {
            return furnace;
        }
        throw new IllegalStateException("Missing alchemical furnace at " + pos);
    }

    public boolean isLit() {
        return data.get(1) > 0;
    }

    public int fuelTime() { return data.get(1); }
    public int currentFuelTime() { return data.get(2); }
    public int burnProgress() { return data.get(0); }
    public int burnDuration() { return data.get(4); }
    public int storedEssentia() { return data.get(3); }
    public int capacity() { return furnace.capacity(); }
    public BlockPos blockPos() { return pos; }

    public int litProgress(int pixels) {
        int current = Math.max(1, currentFuelTime());
        return Math.max(0, Math.min(pixels, fuelTime() * pixels / current));
    }

    public int burnProgress(int pixels) {
        int duration = Math.max(1, burnDuration());
        return Math.max(0, Math.min(pixels, burnProgress() * pixels / duration));
    }

    public int essentiaProgress(int pixels) {
        int max = Math.max(1, capacity());
        return Math.max(0, Math.min(pixels, storedEssentia() * pixels / max));
    }

    @Override
    public boolean stillValid(Player player) {
        return furnace.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return result;

        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else if (furnace.canPlaceItem(AlchemicalFurnaceBlockEntity.SLOT_FUEL, stack)) {
            if (!moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
        } else if (furnace.canPlaceItem(AlchemicalFurnaceBlockEntity.SLOT_INPUT, stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (index < 29) {
            if (!moveItemStackTo(stack, 29, 38, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 2, 29, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return result;
    }
}
