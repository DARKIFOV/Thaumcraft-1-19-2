package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.BottomlessPouchItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BottomlessPouchMenu extends AbstractContainerMenu {
    private final Container pouchContainer;
    private final ItemStack pouchStack;

    public BottomlessPouchMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, data.readBoolean());
    }

    public BottomlessPouchMenu(int containerId, Inventory inventory, boolean mainHand) {
        this(containerId, inventory, mainHand ? inventory.player.getMainHandItem() : inventory.player.getOffhandItem());
    }

    public BottomlessPouchMenu(int containerId, Inventory inventory, ItemStack pouchStack) {
        super(ThaumcraftMod.BOTTOMLESS_POUCH_MENU.get(), containerId);
        this.pouchStack = pouchStack;
        this.pouchContainer = new BottomlessPouchContainer(pouchStack);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(pouchContainer, col + row * 9, 8 + col * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return !(stack.getItem() instanceof BottomlessPouchItem);
                    }
                });
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 144));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return pouchContainer.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < BottomlessPouchContainer.SIZE) {
                if (!moveItemStackTo(stack, BottomlessPouchContainer.SIZE, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (stack.getItem() instanceof BottomlessPouchItem || !moveItemStackTo(stack, 0, BottomlessPouchContainer.SIZE, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        pouchContainer.setChanged();
    }
}
