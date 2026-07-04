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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ArcaneWorkbenchMenu extends AbstractContainerMenu {
    private final Container workbench;
    private final BlockPos blockPos;

    public ArcaneWorkbenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getContainer(playerInventory, data.readBlockPos()));
    }

    public ArcaneWorkbenchMenu(int containerId, Inventory playerInventory, Container workbench) {
        super(ThaumcraftMod.ARCANE_WORKBENCH_MENU.get(), containerId);
        checkContainerSize(workbench, ArcaneWorkbenchBlockEntity.SIZE);
        this.workbench = workbench;
        this.blockPos = workbench instanceof BlockEntity blockEntity ? blockEntity.getBlockPos() : BlockPos.ZERO;
        workbench.startOpen(playerInventory.player);

        addSlot(new Slot(workbench, ArcaneWorkbenchBlockEntity.SLOT_WAND, 16, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof WandItem;
            }
        });

        addSlot(new Slot(workbench, ArcaneWorkbenchBlockEntity.SLOT_CATALYST, 44, 36));

        int index = ArcaneWorkbenchBlockEntity.SLOT_INGREDIENT_START;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(workbench, index++, 80 + col * 18, 18 + row * 18));
            }
        }

        addSlot(new Slot(workbench, ArcaneWorkbenchBlockEntity.SLOT_OUTPUT, 152, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 161));
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

            if (index < ArcaneWorkbenchBlockEntity.SIZE) {
                if (!moveItemStackTo(stack, ArcaneWorkbenchBlockEntity.SIZE, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                int targetStart = stack.getItem() instanceof WandItem
                        ? ArcaneWorkbenchBlockEntity.SLOT_WAND
                        : ArcaneWorkbenchBlockEntity.SLOT_INGREDIENT_START;
                int targetEnd = stack.getItem() instanceof WandItem
                        ? ArcaneWorkbenchBlockEntity.SLOT_WAND + 1
                        : ArcaneWorkbenchBlockEntity.SLOT_INGREDIENT_END + 1;

                if (!moveItemStackTo(stack, targetStart, targetEnd, false)) {
                    if (!moveItemStackTo(stack, ArcaneWorkbenchBlockEntity.SLOT_CATALYST, ArcaneWorkbenchBlockEntity.SLOT_CATALYST + 1, false)) {
                        return ItemStack.EMPTY;
                    }
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
    public void removed(Player player) {
        super.removed(player);
        workbench.stopOpen(player);
    }
}
