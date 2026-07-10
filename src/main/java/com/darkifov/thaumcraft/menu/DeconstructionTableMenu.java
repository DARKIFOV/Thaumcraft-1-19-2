package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.DeconstructionTableBlockEntity;
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

public class DeconstructionTableMenu extends AbstractContainerMenu {
    private final Container table;
    private final ContainerData data;
    private final BlockPos pos;

    public DeconstructionTableMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, resolve(inventory, buffer.readBlockPos()), new SimpleContainerData(2));
    }

    public DeconstructionTableMenu(int id, Inventory inventory, DeconstructionTableBlockEntity table, ContainerData data) {
        super(ThaumcraftMod.DECONSTRUCTION_TABLE_MENU.get(), id);
        this.table = table;
        this.data = data;
        this.pos = table.getBlockPos();
        checkContainerSize(table, 1);
        checkContainerDataCount(data, 2);
        table.startOpen(inventory.player);
        addDataSlots(data);

        addSlot(new Slot(table, 0, 64, 16) {
            @Override public boolean mayPlace(ItemStack stack) {
                return AspectDatabase.getAspectsForItem(stack) != null && !AspectDatabase.getAspectsForItem(stack).isEmpty();
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
    }

    private static DeconstructionTableBlockEntity resolve(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level.getBlockEntity(pos);
        if (blockEntity instanceof DeconstructionTableBlockEntity table) return table;
        throw new IllegalStateException("Missing deconstruction table at " + pos);
    }

    public int breakTime() { return data.get(0); }
    public int scaledBreakTime(int pixels) { return Math.max(0, Math.min(pixels, breakTime() * pixels / DeconstructionTableBlockEntity.BREAK_TICKS)); }
    public Aspect outputAspect() { int value = data.get(1); return value <= 0 || value > Aspect.values().length ? null : Aspect.values()[value - 1]; }
    public BlockPos blockPos() { return pos; }

    @Override public boolean clickMenuButton(Player player, int id) {
        return id == 1 && table instanceof DeconstructionTableBlockEntity deconstruction && deconstruction.claimAspect(player);
    }

    @Override public boolean stillValid(Player player) { return table.stillValid(player); }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return result;
        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!slots.get(0).mayPlace(stack) || !moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return result;
    }

    @Override public void removed(Player player) { super.removed(player); table.stopOpen(player); }
}
