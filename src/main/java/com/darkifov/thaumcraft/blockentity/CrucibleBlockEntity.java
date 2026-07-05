package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CrucibleBlockEntity extends BlockEntity {
    private final AspectList aspects = new AspectList();
    private boolean hasWater = false;
    private int flux = 0;

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.CRUCIBLE_BLOCK_ENTITY.get(), pos, state);
    }

    public AspectList aspects() {
        return aspects;
    }

    public boolean hasWater() {
        return hasWater;
    }

    public void setWater(boolean hasWater) {
        this.hasWater = hasWater;
        setChangedAndSync();
    }

    public int flux() {
        return flux;
    }

    public void addFlux(int amount) {
        flux = Math.max(0, flux + amount);
        setChangedAndSync();
    }

    public void clearFlux() {
        flux = 0;
        setChangedAndSync();
    }

    public void addAspects(AspectList added) {
        aspects.addAll(added);

        if (aspects.totalAmount() > 64) {
            addFlux(2);
        }

        setChangedAndSync();
    }

    public void clearAspects() {
        aspects.clear();
        setChangedAndSync();
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
        tag.putBoolean("HasWater", hasWater);
        tag.putInt("Flux", flux);
        tag.put("Aspects", aspects.save());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        hasWater = tag.getBoolean("HasWater");
        flux = tag.getInt("Flux");

        if (tag.contains("Aspects")) {
            aspects.load(tag.getCompound("Aspects"));
        }
    }
}
