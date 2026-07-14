package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.HandMirrorItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** One-slot immediate-send container matching TC4 ContainerHandMirror coordinates. */
public final class HandMirrorMenu extends AbstractContainerMenu {
    private static final int INPUT_SLOT = 0;
    private static final int PLAYER_START = 1;
    private final SimpleContainer input = new SimpleContainer(1);
    private final ItemStack mirrorStack;
    private final Player player;
    private boolean processing;

    public HandMirrorMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, data.readBoolean()
                ? inventory.player.getMainHandItem() : inventory.player.getOffhandItem());
    }

    public HandMirrorMenu(int containerId, Inventory inventory, ItemStack mirrorStack) {
        super(ThaumcraftMod.HAND_MIRROR_MENU.get(), containerId);
        this.mirrorStack = mirrorStack;
        this.player = inventory.player;

        addSlot(new Slot(input, 0, 80, 24) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !(stack.getItem() instanceof HandMirrorItem);
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
        input.addListener(this::slotsChanged);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (processing || container != input || input.getItem(0).isEmpty()) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack pending = input.getItem(0);
        if (ItemStack.isSameItemSameTags(pending, mirrorStack)) {
            serverPlayer.closeContainer();
            return;
        }
        processing = true;
        try {
            if (HandMirrorItem.transport(mirrorStack, pending, serverPlayer)) {
                input.setItem(0, ItemStack.EMPTY);
                broadcastChanges();
            }
        } finally {
            processing = false;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive() && mirrorStack.getItem() instanceof HandMirrorItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem() || slot.getItem().getItem() instanceof HandMirrorItem) {
            return ItemStack.EMPTY;
        }
        ItemStack current = slot.getItem();
        ItemStack original = current.copy();
        if (index == INPUT_SLOT) {
            if (!moveItemStackTo(current, PLAYER_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(current, INPUT_SLOT, INPUT_SLOT + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (current.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level.isClientSide) {
            return;
        }
        ItemStack remaining = input.removeItemNoUpdate(0);
        if (!remaining.isEmpty() && !player.getInventory().add(remaining)) {
            player.drop(remaining, false);
        }
    }
}
