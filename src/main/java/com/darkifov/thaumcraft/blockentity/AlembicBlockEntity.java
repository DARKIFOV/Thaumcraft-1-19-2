package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AlembicBlockEntity extends BlockEntity {
    public static final int CAPACITY = 32;

    private final AspectList aspects = new AspectList();

    public AlembicBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ALEMBIC_BLOCK_ENTITY.get(), pos, state);
    }

    public AspectList aspects() {
        return aspects;
    }

    public int spaceLeft() {
        return Math.max(0, CAPACITY - aspects.totalAmount());
    }

    public boolean canAccept(Aspect aspect) {
        Aspect first = aspects.firstAspect();
        return first == null || first == aspect;
    }

    public int addEssentia(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0 || !canAccept(aspect)) {
            return 0;
        }

        int added = Math.min(amount, spaceLeft());

        if (added > 0) {
            aspects.add(aspect, added);
            setChangedAndSync();
        }

        return added;
    }

    public int removeEssentia(Aspect aspect, int amount) {
        int removed = aspects.removeUpTo(aspect, amount);

        if (removed > 0) {
            setChangedAndSync();
        }

        return removed;
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
        tag.put("Aspects", aspects.save());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("Aspects")) {
            aspects.load(tag.getCompound("Aspects"));
        }
    }
}
