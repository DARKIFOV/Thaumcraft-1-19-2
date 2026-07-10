package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TemporaryHoleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

/**
 * Modern TileHole/TileMemory adapter.
 *
 * The old temporary-hole implementation discarded every replaced block.  TC4
 * stores the exact old block state, grows a 3x3 passage one layer per tick and
 * restores every layer after the focus duration expires.  This block entity
 * preserves that lifecycle and survives chunk save/load.
 */
public class TemporaryHoleBlockEntity extends BlockEntity {
    private BlockState rememberedState = Blocks.AIR.defaultBlockState();
    private int age;
    private int duration = 120;
    private int remainingLayers = 1;
    private Direction clickedFace;
    private boolean expanded;
    private UUID owner = new UUID(0L, 0L);

    public TemporaryHoleBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.TEMPORARY_HOLE_BLOCK_ENTITY.get(), pos, state);
    }

    public void initialize(BlockState rememberedState, int duration, int remainingLayers, Direction clickedFace, UUID owner) {
        this.rememberedState = sanitizeRememberedState(rememberedState);
        this.duration = Math.max(1, duration);
        this.remainingLayers = Math.max(1, remainingLayers);
        this.clickedFace = clickedFace;
        this.age = 0;
        this.expanded = false;
        this.owner = owner == null ? new UUID(0L, 0L) : owner;
        setChangedAndSync();
    }

    public BlockState rememberedState() {
        return rememberedState;
    }

    public int age() {
        return age;
    }

    public int duration() {
        return duration;
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, TemporaryHoleBlockEntity hole) {
        if (!hole.expanded) {
            // Modern protection safety: delayed chunk reload must not expand the
            // 3x3 tunnel without the original casting player being available.
            if (hole.hasOwner() && hole.resolveOwner(level) == null) {
                hole.age++;
                if (hole.age >= hole.duration) hole.restore(level, pos);
                else if ((hole.age & 7) == 0) hole.setChanged();
                return;
            }
            hole.expanded = true;
            hole.expandOriginalTunnel(level, pos);
            hole.setChanged();
        }

        hole.age++;
        if (hole.age >= hole.duration) {
            hole.restore(level, pos);
        } else if ((hole.age & 7) == 0) {
            hole.setChanged();
        }
    }

    private void expandOriginalTunnel(ServerLevel level, BlockPos pos) {
        if (clickedFace == null || remainingLayers <= 1) {
            return;
        }

        // TileHole creates the eight surrounding cells of the current plane.
        Direction.Axis axis = clickedFace.getAxis();
        for (int first = -1; first <= 1; first++) {
            for (int second = -1; second <= 1; second++) {
                if (first == 0 && second == 0) continue;
                BlockPos sidePos = switch (axis) {
                    case Y -> pos.offset(first, 0, second);
                    case Z -> pos.offset(first, second, 0);
                    case X -> pos.offset(0, first, second);
                };
                TemporaryHoleBlock.createHole(level, sidePos, duration, 1, null, resolveOwner(level));
            }
        }

        // Original side IDs advance in the direction opposite the clicked face.
        BlockPos next = pos.relative(clickedFace.getOpposite());
        TemporaryHoleBlock.createHole(level, next, duration, remainingLayers - 1, clickedFace, resolveOwner(level));
    }


    private boolean hasOwner() {
        return owner.getMostSignificantBits() != 0L || owner.getLeastSignificantBits() != 0L;
    }

    private net.minecraft.server.level.ServerPlayer resolveOwner(ServerLevel level) {
        return hasOwner() ? level.getServer().getPlayerList().getPlayer(owner) : null;
    }

    private void restore(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(ThaumcraftMod.TEMPORARY_HOLE.get())) {
            return;
        }
        BlockState restore = sanitizeRememberedState(rememberedState);
        level.setBlock(pos, restore, Block.UPDATE_ALL);
        level.getChunkSource().getLightEngine().checkBlock(pos);
        level.scheduleTick(pos, restore.getBlock(), 2);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                5, 0.35D, 0.35D, 0.35D, 0.02D);
    }

    private static BlockState sanitizeRememberedState(BlockState state) {
        if (state == null || state.isAir()
                || state.is(ThaumcraftMod.TEMPORARY_HOLE.get())
                || state.is(ThaumcraftMod.WARDED_BLOCK.get())) {
            return Blocks.AIR.defaultBlockState();
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
        tag.putInt("Age", age);
        tag.putInt("Duration", duration);
        tag.putInt("RemainingLayers", remainingLayers);
        tag.putBoolean("Expanded", expanded);
        tag.putUUID("Owner", owner);
        if (clickedFace != null) {
            tag.putInt("ClickedFace", clickedFace.get3DDataValue());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        rememberedState = sanitizeRememberedState(tag.contains("RememberedState", 10)
                ? NbtUtils.readBlockState(tag.getCompound("RememberedState"))
                : Blocks.AIR.defaultBlockState());
        age = Math.max(0, tag.getInt("Age"));
        duration = Math.max(1, tag.contains("Duration") ? tag.getInt("Duration") : 120);
        remainingLayers = Math.max(1, tag.contains("RemainingLayers") ? tag.getInt("RemainingLayers") : 1);
        expanded = tag.getBoolean("Expanded");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : new UUID(0L, 0L);
        clickedFace = tag.contains("ClickedFace") ? Direction.from3DDataValue(tag.getInt("ClickedFace")) : null;
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
