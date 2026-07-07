package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.blockentity.ArcaneWorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ArcaneWorkbenchMenu extends AbstractContainerMenu {
    private final Container workbench;
    private final Inventory playerInventory;
    private final BlockPos blockPos;

    /** Menu indices mirror original TC4 ContainerArcaneWorkbench. */
    public static final int MENU_SLOT_OUTPUT = 0;
    public static final int MENU_SLOT_WAND = 1;
    public static final int MENU_SLOT_GRID_START = 2;
    public static final int MENU_SLOT_GRID_END = 10;
    public static final int MENU_PLAYER_INV_START = 11;
    public static final int MENU_PLAYER_INV_END = 37;
    public static final int MENU_HOTBAR_START = 38;
    public static final int MENU_HOTBAR_END = 46;

    public ArcaneWorkbenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getContainer(playerInventory, data.readBlockPos()));
    }

    public ArcaneWorkbenchMenu(int containerId, Inventory playerInventory, Container workbench) {
        super(ThaumcraftMod.ARCANE_WORKBENCH_MENU.get(), containerId);
        checkContainerSize(workbench, ArcaneWorkbenchBlockEntity.SIZE);
        this.workbench = workbench;
        this.playerInventory = playerInventory;
        this.blockPos = workbench instanceof BlockEntity blockEntity ? blockEntity.getBlockPos() : BlockPos.ZERO;
        workbench.startOpen(playerInventory.player);

        addSlot(new Slot(workbench, ArcaneWorkbenchBlockEntity.SLOT_OUTPUT, 160, 64) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                if (workbench instanceof ArcaneWorkbenchBlockEntity arcaneWorkbench) {
                    arcaneWorkbench.craftFromOutput(player);
                }
            }
        });

        addSlot(new Slot(workbench, ArcaneWorkbenchBlockEntity.SLOT_WAND, 160, 24) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof WandItem && !WandItem.isStaffStack(stack);
            }
        });

        int index = ArcaneWorkbenchBlockEntity.SLOT_INGREDIENT_START;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(workbench, index++, 40 + col * 24, 40 + row * 24));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 16 + col * 18, 151 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 16 + col * 18, 209));
        }

        if (workbench instanceof ArcaneWorkbenchBlockEntity arcaneWorkbench) {
            arcaneWorkbench.updateOutputPreview(playerInventory.player);
        }
    }

    private static Container getContainer(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level.getBlockEntity(pos);

        if (blockEntity instanceof ArcaneWorkbenchBlockEntity workbench) {
            return workbench;
        }

        throw new IllegalStateException("Arcane Workbench block entity missing at " + pos);
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == workbench && workbench instanceof ArcaneWorkbenchBlockEntity arcaneWorkbench) {
            arcaneWorkbench.updateOutputPreview(playerInventory.player);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return workbench.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();

            if (index == MENU_SLOT_OUTPUT) {
                // Original func_82846_b: output only moves into player inventory + hotbar (11..47).
                if (!moveItemStackTo(stack, MENU_PLAYER_INV_START, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, original);
            } else if (index >= MENU_PLAYER_INV_START && index <= MENU_PLAYER_INV_END) {
                // Original TC4 does not shift-click ordinary inventory stacks into the 3x3 grid.
                if (stack.getItem() instanceof WandItem && !WandItem.isStaffStack(stack)) {
                    if (!moveItemStackTo(stack, MENU_SLOT_WAND, MENU_SLOT_WAND + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, MENU_HOTBAR_START, MENU_HOTBAR_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= MENU_HOTBAR_START && index <= MENU_HOTBAR_END) {
                if (stack.getItem() instanceof WandItem && !WandItem.isStaffStack(stack)) {
                    if (!moveItemStackTo(stack, MENU_SLOT_WAND, MENU_SLOT_WAND + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, MENU_PLAYER_INV_START, MENU_PLAYER_INV_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Grid and wand slot move back to inventory/hotbar, mirroring TC4's final branch.
                if (!moveItemStackTo(stack, MENU_PLAYER_INV_START, slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == original.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return original;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        // Original func_75144_a: drop-click mode forces dragType=1, and right-clicks
        // on output/wand slots are coerced to left-click semantics.
        if (clickType == ClickType.THROW) {
            dragType = 1;
        }
        if ((slotId == MENU_SLOT_OUTPUT || slotId == MENU_SLOT_WAND) && dragType > 0) {
            dragType = 0;
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        // Original func_94530_a rejects drag-splitting into TileArcaneWorkbench slots.
        return slot.container != workbench && super.canDragTo(slot);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        workbench.stopOpen(player);
    }
}
