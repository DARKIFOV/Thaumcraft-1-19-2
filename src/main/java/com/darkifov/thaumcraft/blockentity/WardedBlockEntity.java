package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/** Persistent modern equivalent of TC4 TileWarded. */
public class WardedBlockEntity extends BlockEntity {
    private BlockState rememberedState = Blocks.STONE.defaultBlockState();
    private UUID owner = new UUID(0L, 0L);

    public WardedBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.WARDED_BLOCK_ENTITY.get(), pos, state);
    }

    public void initialize(BlockState rememberedState, UUID owner) {
        this.rememberedState = sanitizeRememberedState(rememberedState);
        this.owner = owner == null ? new UUID(0L, 0L) : owner;
        setChangedAndSync();
    }

    public BlockState rememberedState() {
        return rememberedState;
    }

    public UUID owner() {
        return owner;
    }

    public boolean isOwner(UUID candidate) {
        return candidate != null && owner.equals(candidate);
    }

    private static BlockState sanitizeRememberedState(BlockState state) {
        if (state == null || state.isAir()
                || state.is(ThaumcraftMod.WARDED_BLOCK.get())
                || state.is(ThaumcraftMod.TEMPORARY_HOLE.get())) {
            return Blocks.STONE.defaultBlockState();
        }
        return state;
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            if (level instanceof ServerLevel server) server.getChunkSource().getLightEngine().checkBlock(worldPosition);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("RememberedState", NbtUtils.writeBlockState(rememberedState));
        tag.putUUID("Owner", owner);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        rememberedState = sanitizeRememberedState(tag.contains("RememberedState", 10)
                ? NbtUtils.readBlockState(tag.getCompound("RememberedState"))
                : Blocks.STONE.defaultBlockState());
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : new UUID(0L, 0L);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) load(tag);
    }
}
