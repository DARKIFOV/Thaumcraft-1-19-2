package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.item.gear.HoverHarnessItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

/** One-jar inventory from TC4 ContainerHoverHarness/InventoryHoverHarness. */
public final class HoverHarnessMenu extends AbstractContainerMenu {
    private static final int JAR_SLOTS = 1;
    private final Inventory playerInventory;
    private final InteractionHand hand;
    private final SimpleContainer jarInventory;
    private final int lockedMenuSlot;

    public HoverHarnessMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, data.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public HoverHarnessMenu(int containerId, Inventory inventory, InteractionHand hand) {
        super(ThaumcraftMod.HOVER_HARNESS_MENU.get(), containerId);
        this.playerInventory = inventory;
        this.hand = hand;
        this.jarInventory = new SimpleContainer(JAR_SLOTS);
        this.lockedMenuSlot = hand == InteractionHand.MAIN_HAND ? 28 + inventory.selected : -1;

        ItemStack source = sourceStack();
        if (source.getItem() instanceof HoverHarnessItem) {
            jarInventory.setItem(0, HoverHarnessItem.getJar(source));
        }
        jarInventory.addListener(this::slotsChanged);

        addSlot(new Slot(jarInventory, 0, 80, 32) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return HoverHarnessItem.isValidFuelJar(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        saveJar();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex == lockedMenuSlot || slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack raw = slot.getItem();
        ItemStack copy = raw.copy();
        if (slotIndex == 0) {
            if (!moveItemStackTo(raw, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!HoverHarnessItem.isValidFuelJar(raw) || !moveItemStackTo(raw, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (raw.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        saveJar();
        return copy;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (slotId == lockedMenuSlot) {
            return;
        }
        super.clicked(slotId, dragType, clickType, player);
        saveJar();
    }

    @Override
    public boolean stillValid(Player player) {
        return sourceStack().getItem() instanceof HoverHarnessItem;
    }

    @Override
    public void removed(Player player) {
        saveJar();
        super.removed(player);
    }

    private ItemStack sourceStack() {
        return hand == InteractionHand.MAIN_HAND
                ? playerInventory.player.getMainHandItem()
                : playerInventory.player.getOffhandItem();
    }

    private void saveJar() {
        if (playerInventory.player.level.isClientSide) {
            return;
        }
        ItemStack source = sourceStack();
        if (source.getItem() instanceof HoverHarnessItem) {
            HoverHarnessItem.setJar(source, jarInventory.getItem(0));
            playerInventory.setChanged();
        }
    }
}
