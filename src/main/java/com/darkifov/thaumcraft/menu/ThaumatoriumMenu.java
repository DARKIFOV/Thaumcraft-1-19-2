package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Stage523-542 Forge 1.19.2 adapter for original TC4 ContainerThaumatorium.
 *
 * TC4's construct does not expose a vanilla 3x3 crafting grid.  This menu only
 * anchors the original GUI and player inventory while the tile keeps catalyst,
 * formula and essentia-network state.  No recipe browser or modern automation
 * behaviour is invented here.
 */
public class ThaumatoriumMenu extends AbstractContainerMenu {
    public static final int PLAYER_INV_START = 0;
    public static final int PLAYER_INV_END = 26;
    public static final int HOTBAR_START = 27;
    public static final int HOTBAR_END = 35;

    private final Inventory playerInventory;
    private final BlockPos blockPos;

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

        // Original GUI keeps the inventory low on the alchemical construct screen.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 132 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 190));
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
        // Original ContainerThaumatorium has no grid insertion through shift-click.
        // Keep shift-click as a no-op until the complete original slot logic is ported.
        return ItemStack.EMPTY;
    }
}
