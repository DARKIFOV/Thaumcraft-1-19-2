package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Persistent node profile and the one-second TC4 jar-collapse animation. */
public final class NodeJarBlockEntity extends BlockEntity {
    private static final String TAG_NODE = "Node";
    private static final String TAG_ANIMATION_END = "CaptureAnimationEnd";
    private CompoundTag nodeTag = new CompoundTag();
    private long captureAnimationEnd;

    public NodeJarBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.NODE_JAR_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean hasNode() {
        return !nodeTag.isEmpty() && nodeTag.contains("Aspects");
    }

    public CompoundTag nodeTag() {
        return nodeTag.copy();
    }

    public void setNodeTag(CompoundTag value) {
        nodeTag = value == null ? new CompoundTag() : value.copy();
        setChangedAndSync();
    }

    public void startCaptureAnimation() {
        captureAnimationEnd = level == null ? 20L : level.getGameTime() + 20L;
        setChangedAndSync();
    }

    /** Exact TileJarRenderer scale: 1 + 2 * remainingSeconds. */
    public float captureScale(float partialTick) {
        if (level == null || captureAnimationEnd <= 0L) {
            return 1.0F;
        }
        float remainingTicks = captureAnimationEnd - (level.getGameTime() + partialTick);
        if (remainingTicks <= 0.0F) {
            return 1.0F;
        }
        return 1.0F + 2.0F * Math.min(1.0F, remainingTicks / 20.0F);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(2.0D);
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_NODE, nodeTag.copy());
        tag.putLong(TAG_ANIMATION_END, captureAnimationEnd);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        nodeTag = tag.contains(TAG_NODE) ? tag.getCompound(TAG_NODE).copy() : new CompoundTag();
        captureAnimationEnd = tag.getLong(TAG_ANIMATION_END);
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
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
