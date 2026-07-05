package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaJarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EssentiaJarBlockEntity extends BlockEntity {
    private final AspectList aspects = new AspectList();
    private Aspect filterAspect = null;

    public EssentiaJarBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ESSENTIA_JAR_BLOCK_ENTITY.get(), pos, state);
    }

    public AspectList aspects() {
        return aspects;
    }

    public Aspect filterAspect() {
        return filterAspect;
    }

    public boolean hasFilter() {
        return filterAspect != null;
    }

    public void setFilterAspect(Aspect aspect) {
        filterAspect = aspect;
        setChangedAndSync();
    }

    public void clearFilter() {
        filterAspect = null;
        setChangedAndSync();
    }

    public boolean canAcceptAspect(Aspect aspect) {
        if (aspect == null) {
            return false;
        }

        if (filterAspect != null && filterAspect != aspect) {
            return false;
        }

        Aspect current = aspects.firstAspect();

        return current == null || current == aspect;
    }

    public int acceptFromTube(Aspect aspect, int amount, boolean voidOverflow) {
        if (!canAcceptAspect(aspect) || amount <= 0) {
            return 0;
        }

        if (filterAspect == null && getBlockState().is(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get())) {
            filterAspect = aspect;
        }

        int space = Math.max(0, EssentiaJarBlock.CAPACITY - aspects.totalAmount());
        int accepted = Math.min(space, amount);

        if (accepted > 0) {
            aspects.add(aspect, accepted);
        }

        int consumed = accepted;

        if (voidOverflow && accepted < amount) {
            consumed = amount;
        }

        if (consumed > 0) {
            setChangedAndSync();
        }

        return consumed;
    }


    public Aspect storedAspect() {
        return aspects.firstAspect();
    }

    public int amount() {
        return aspects.totalAmount();
    }

    public int capacity() {
        return EssentiaJarBlock.CAPACITY;
    }

    public float fillRatio() {
        return Math.max(0.0F, Math.min(1.0F, aspects.totalAmount() / (float) EssentiaJarBlock.CAPACITY));
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

        if (filterAspect != null) {
            tag.putString("FilterAspect", filterAspect.name());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("Aspects")) {
            aspects.load(tag.getCompound("Aspects"));
        }

        filterAspect = null;

        if (tag.contains("FilterAspect")) {
            try {
                filterAspect = Aspect.valueOf(tag.getString("FilterAspect"));
            } catch (IllegalArgumentException ignored) {
                filterAspect = null;
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }
}
