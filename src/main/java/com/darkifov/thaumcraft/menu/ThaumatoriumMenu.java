package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ThaumatoriumBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;

/**
 * Stage523-542 Forge 1.19.2 adapter for original TC4 ContainerThaumatorium.
 *
 * TC4's construct does not expose a vanilla 3x3 crafting grid.  This menu only
 * anchors the original GUI and player inventory while the tile keeps catalyst,
 * formula and essentia-network state.  No recipe browser or modern automation
 * behaviour is invented here.
 */
public class ThaumatoriumMenu extends AbstractContainerMenu {
    public static final int CATALYST_SLOT = 0;
    public static final int PLAYER_INV_START = 1;
    public static final int PLAYER_INV_END = 27;
    public static final int HOTBAR_START = 28;
    public static final int HOTBAR_END = 36;

    private final Inventory playerInventory;
    private final BlockPos blockPos;
    private final Container thaumatorium;

    public ThaumatoriumMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readBlockPos());
    }

    public ThaumatoriumMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity == null ? BlockPos.ZERO : blockEntity.getBlockPos());
    }

    public ThaumatoriumMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        super(ThaumcraftMod.THAUMATORIUM_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.blockPos = blockPos == null ? BlockPos.ZERO : blockPos;
        BlockEntity blockEntity = playerInventory.player.level.getBlockEntity(this.blockPos);
        this.thaumatorium = blockEntity instanceof ThaumatoriumBlockEntity tile ? tile : new SimpleContainer(1);
        addSlot(new Slot(this.thaumatorium, 0, 48, 16));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return player != null
                && player.level.getBlockState(blockPos).is(ThaumcraftMod.THAUMATORIUM.get())
                && player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        if (index == CATALYST_SLOT) {
            if (!moveItemStackTo(source, PLAYER_INV_START, HOTBAR_END + 1, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, CATALYST_SLOT, CATALYST_SLOT + 1, false)) {
            if (index >= PLAYER_INV_START && index <= PLAYER_INV_END) {
                if (!moveItemStackTo(source, HOTBAR_START, HOTBAR_END + 1, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(source, PLAYER_INV_START, PLAYER_INV_END + 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (source.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, source);
        return copy;
    }
}
