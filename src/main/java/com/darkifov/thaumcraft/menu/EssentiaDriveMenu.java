package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaCellItem;
import com.darkifov.thaumcraft.block.EssentiaUpgradeCardItem;
import com.darkifov.thaumcraft.blockentity.EssentiaDriveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EssentiaDriveMenu extends AbstractContainerMenu {
    private final Container drive;
    private final BlockPos blockPos;

    public EssentiaDriveMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getContainer(playerInventory, data.readBlockPos()));
    }

    public EssentiaDriveMenu(int containerId, Inventory playerInventory, Container drive) {
        super(ThaumcraftMod.ESSENTIA_DRIVE_MENU.get(), containerId);
        checkContainerSize(drive, EssentiaDriveBlockEntity.SIZE);
        this.drive = drive;
        this.blockPos = drive instanceof BlockEntity blockEntity ? blockEntity.getBlockPos() : BlockPos.ZERO;
        drive.startOpen(playerInventory.player);

        for (int i = 0; i < EssentiaDriveBlockEntity.CELL_SLOTS; i++) {
            int row = i / 5;
            int col = i % 5;

            addSlot(new Slot(drive, i, 17 + col * 18, 22 + row * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof EssentiaCellItem;
                }
            });
        }

        for (int i = EssentiaDriveBlockEntity.UPGRADE_START; i <= EssentiaDriveBlockEntity.UPGRADE_END; i++) {
            int col = i - EssentiaDriveBlockEntity.UPGRADE_START;

            addSlot(new Slot(drive, i, 124 + col * 18, 22) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof EssentiaUpgradeCardItem;
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 118 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 176));
        }
    }

    private static Container getContainer(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level.getBlockEntity(pos);

        if (blockEntity instanceof Container container) {
            return container;
        }

        throw new IllegalStateException("Expected Essentia Drive container at " + pos);
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return drive.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();

            int driveSlots = EssentiaDriveBlockEntity.SIZE;

            if (index < driveSlots) {
                if (!moveItemStackTo(stack, driveSlots, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getItem() instanceof EssentiaCellItem) {
                if (!moveItemStackTo(stack, 0, EssentiaDriveBlockEntity.CELL_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getItem() instanceof EssentiaUpgradeCardItem) {
                if (!moveItemStackTo(stack, EssentiaDriveBlockEntity.UPGRADE_START, EssentiaDriveBlockEntity.UPGRADE_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        drive.stopOpen(player);
    }
}
