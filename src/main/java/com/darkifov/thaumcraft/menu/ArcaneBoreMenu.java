package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandFocusItem;
import com.darkifov.thaumcraft.blockentity.ArcaneBoreBlockEntity;
import com.darkifov.thaumcraft.blockentity.TC4ArcaneBoreParity;
import com.darkifov.thaumcraft.wand.WandFocusType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraftforge.items.SlotItemHandler;

/** Exact two-slot TC4 ContainerArcaneBore contract. */
public final class ArcaneBoreMenu extends AbstractContainerMenu {
    private final ArcaneBoreBlockEntity bore;
    private final ContainerData data;

    public ArcaneBoreMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, resolve(inventory, buffer.readBlockPos()), new SimpleContainerData(8));
    }

    public ArcaneBoreMenu(int id, Inventory inventory, ArcaneBoreBlockEntity bore, ContainerData data) {
        super(ThaumcraftMod.ARCANE_BORE_MENU.get(), id);
        this.bore = bore;
        this.data = data;
        checkContainerDataCount(data, 8);
        addDataSlots(data);
        addSlot(new SlotItemHandler(bore.inventory(), 0, 26, 18) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof WandFocusItem focus && focus.focusType() == WandFocusType.EXCAVATION;
            }
        });
        addSlot(new SlotItemHandler(bore.inventory(), 1, 74, 18) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof PickaxeItem; }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 59 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 117));
    }

    private static ArcaneBoreBlockEntity resolve(Inventory inventory, BlockPos pos) {
        if (inventory.player.level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) return bore;
        throw new IllegalStateException("Missing Arcane Bore at " + pos);
    }

    public int width() { return TC4ArcaneBoreParity.width(data.get(0)); }
    public int speed() { return data.get(1); }
    public int fortune() { return data.get(2); }
    public boolean silkTouch() { return data.get(3) != 0; }
    public boolean accelerated() { return data.get(4) > 0; }
    public boolean nativeClusters() { return data.get(6) != 0; }
    public boolean pickaxeNearBroken() { return data.get(7) != 0; }

    @Override public boolean stillValid(Player player) { return bore.stillValid(player); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof WandFocusItem focus && focus.focusType() == WandFocusType.EXCAVATION) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof PickaxeItem) {
            if (!moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return result;
    }
}
