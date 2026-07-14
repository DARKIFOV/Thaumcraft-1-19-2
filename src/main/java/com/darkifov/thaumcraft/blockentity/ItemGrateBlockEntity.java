package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ItemGrateBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TC4 TileGrate parity adapter. The original tile exposed one insertion-only
 * inventory slot on its upper face and immediately spawned the inserted stack
 * as a downward-moving item entity. No stack is retained inside the block.
 */
public class ItemGrateBlockEntity extends BlockEntity {
    private final IItemHandler insertionHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || !isOpen()) {
                return stack;
            }
            if (!simulate && level != null && !level.isClientSide) {
                ItemStack ejected = stack.copy();
                ItemEntity entity = new ItemEntity(
                        level,
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.6D,
                        worldPosition.getZ() + 0.5D,
                        ejected
                );
                entity.setDeltaMovement(0.0D, -0.1D, 0.0D);
                level.addFreshEntity(entity);
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0 && isOpen();
        }
    };

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> insertionHandler);

    public ItemGrateBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ITEM_GRATE_BLOCK_ENTITY.get(), pos, state);
    }

    private boolean isOpen() {
        BlockState state = getBlockState();
        return state.hasProperty(ItemGrateBlock.OPEN) && state.getValue(ItemGrateBlock.OPEN);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && side == Direction.UP && isOpen()) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandler = LazyOptional.of(() -> insertionHandler);
    }
}
