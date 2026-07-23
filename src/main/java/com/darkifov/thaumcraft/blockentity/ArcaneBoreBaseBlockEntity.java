package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ArcaneBoreBaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Exact TC4 TileArcaneBoreBase suction/nozzle contract. */
public final class ArcaneBoreBaseBlockEntity extends BlockEntity {
    public static final int SUCTION = 128;
    private Direction pendingOrientation;

    public ArcaneBoreBaseBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_BORE_BASE_BLOCK_ENTITY.get(), pos, state);
    }

    public Direction outputFace() {
        return getBlockState().getValue(ArcaneBoreBaseBlock.FACING);
    }

    /** Original canInputFrom returns true on every face; the nozzle is disabled by zero suction only. */
    public boolean canInputFrom(Direction face) {
        return true;
    }

    public int suctionAmount(Direction face) {
        return face == outputFace() ? 0 : SUCTION;
    }

    public Aspect suctionType(Direction face) {
        return Aspect.PERDITIO;
    }

    /**
     * TileArcaneBoreBase.drawEssentia: scan directions in Forge order, stop on the first
     * connectable source that cannot output, and consume one Perditio only below suction 128.
     */
    public boolean tryDrawPerditio() {
        if (level == null || level.isClientSide) return false;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof EssentiaTubeBlockEntity tube) {
                Direction tubeFace = direction.getOpposite();
                if (!tube.allowsOutputTo(tubeFace)) return false;
                if (tube.getSuctionAmount(tubeFace) < suctionAmount(direction)
                        && tube.takeEssentiaOriginal(Aspect.PERDITIO, 1, tubeFace) == 1) {
                    markAndSync();
                    return true;
                }
            }
        }
        return false;
    }

    public void markAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TC4ArcaneBoreParity.NBT_ORIENTATION, outputFace().get3DDataValue());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        pendingOrientation = tag.contains(TC4ArcaneBoreParity.NBT_ORIENTATION, Tag.TAG_INT)
                ? Direction.from3DDataValue(tag.getInt(TC4ArcaneBoreParity.NBT_ORIENTATION)) : null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null || pendingOrientation == null) return;
        BlockState state = getBlockState();
        if (state.hasProperty(ArcaneBoreBaseBlock.FACING)
                && state.getValue(ArcaneBoreBaseBlock.FACING) != pendingOrientation) {
            level.setBlock(worldPosition, state.setValue(ArcaneBoreBaseBlock.FACING, pendingOrientation), 2);
        }
        pendingOrientation = null;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }
}
