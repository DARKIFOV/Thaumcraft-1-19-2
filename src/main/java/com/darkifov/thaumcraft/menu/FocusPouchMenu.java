package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.FocusPouchItem;
import com.darkifov.thaumcraft.block.WandFocusItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Stage186: ContainerFocusPouch slot layout parity. */
public class FocusPouchMenu extends AbstractContainerMenu {
    private final FocusPouchContainer input;
    private final ItemStack pouch;
    private final int blockSlot;

    public FocusPouchMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, data.readBoolean() ? inventory.player.getMainHandItem() : inventory.player.getOffhandItem());
    }

    public FocusPouchMenu(int containerId, Inventory inventory, ItemStack pouch) {
        super(ThaumcraftMod.FOCUS_POUCH_MENU.get(), containerId);
        this.pouch = pouch;
        this.input = new FocusPouchContainer(pouch);
        this.blockSlot = inventory.selected + 45;

        // Original ContainerFocusPouch: 18 SlotLimitedByClass(ItemFocusBasic.class) slots at 37 + a % 6 * 18, 51 + a / 6 * 18.
        for (int a = 0; a < FocusPouchContainer.SIZE; a++) {
            addSlot(new Slot(input, a, 37 + a % 6 * 18, 51 + a / 6 * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof WandFocusItem;
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }

        // Original bindPlayerInventory: 8 + j * 18, 151 + i * 18.
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 151 + i * 18));
            }
        }
        // Original hotbar: 8 + i * 18, 209.
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inventory, i, 8 + i * 18, 209));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        if (slot == blockSlot) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = slots.get(slot);
        if (slotObject != null && slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();
            if (slot < FocusPouchContainer.SIZE) {
                if (!input.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, FocusPouchContainer.SIZE, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!input.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 0, FocusPouchContainer.SIZE, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slotObject.set(ItemStack.EMPTY);
            } else {
                slotObject.setChanged();
            }
        }
        return stack;
    }

    @Override
    public void clicked(int slotId, int dragType, net.minecraft.world.inventory.ClickType clickType, Player player) {
        if (slotId == blockSlot) {
            return;
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public boolean stillValid(Player player) {
        return input.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        input.setChanged();
        player.getInventory().setChanged();
    }

    public ItemStack pouch() {
        return pouch;
    }
}
