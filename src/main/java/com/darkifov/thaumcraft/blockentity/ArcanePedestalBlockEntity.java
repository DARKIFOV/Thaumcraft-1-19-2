package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ArcanePedestalBlockEntity extends BlockEntity {
    private ItemStack stored = ItemStack.EMPTY;

    public ArcanePedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_PEDESTAL_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack stored() {
        return stored;
    }

    public boolean isEmpty() {
        return stored.isEmpty();
    }

    public void setStored(ItemStack stack) {
        if (stack.isEmpty()) {
            stored = ItemStack.EMPTY;
        } else {
            stored = stack.copy();
            stored.setCount(1);
        }

        setChangedAndSync();
    }

    public ItemStack takeStored() {
        ItemStack result = stored.copy();
        stored = ItemStack.EMPTY;
        setChangedAndSync();
        return result;
    }

    public void setChangedAndSync() {
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (!stored.isEmpty()) {
            tag.put("Stored", stored.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("Stored")) {
            stored = ItemStack.of(tag.getCompound("Stored"));
        } else {
            stored = ItemStack.EMPTY;
        }
    }
}
