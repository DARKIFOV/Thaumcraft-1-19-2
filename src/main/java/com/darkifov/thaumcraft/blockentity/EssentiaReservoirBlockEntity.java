package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaReservoirBlock;
import com.darkifov.thaumcraft.essentia.EssentiaSuction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Stage503-522 TileEssentiaReservoir parity adapter. */
public class EssentiaReservoirBlockEntity extends BlockEntity {
    public static final int CAPACITY = 256;
    public static final int ORIGINAL_RESERVOIR_SUCTION = 24;
    public static final int ORIGINAL_FILL_INTERVAL_TICKS = 5;
    public static final String NBT_ASPECTS = "Aspects";
    public static final String NBT_FACING = "facing";

    private final AspectList aspects = new AspectList();
    private Direction facing = Direction.DOWN;
    private int originalTickCounter;

    public EssentiaReservoirBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ESSENTIA_RESERVOIR_BLOCK_ENTITY.get(), pos, state);
        if (state.hasProperty(EssentiaReservoirBlock.FACING)) {
            facing = state.getValue(EssentiaReservoirBlock.FACING);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EssentiaReservoirBlockEntity reservoir) {
        if (state.hasProperty(EssentiaReservoirBlock.FACING) && reservoir.facing != state.getValue(EssentiaReservoirBlock.FACING)) {
            reservoir.facing = state.getValue(EssentiaReservoirBlock.FACING);
            reservoir.setChanged();
        }
        if (level == null || level.isClientSide) {
            return;
        }
        reservoir.originalTickCounter++;
        if (reservoir.originalTickCounter % ORIGINAL_FILL_INTERVAL_TICKS == 0 && reservoir.amount() < CAPACITY) {
            reservoir.fillFromFacingLikeTC4();
        }
    }

    public AspectList aspects() {
        return aspects;
    }

    public Direction facing() {
        return facing;
    }

    public void setFacing(Direction facing) {
        if (facing != null) {
            this.facing = facing;
            setChangedAndSync();
        }
    }

    /** Original reservoir can only be accessed from its chosen side. */
    public boolean canAccessFrom(Direction sideFromReservoir) {
        return sideFromReservoir != null && sideFromReservoir == facing;
    }

    public boolean canAcceptAspect(Aspect aspect) {
        return aspect != null && amount() < CAPACITY;
    }

    public int acceptFromTube(Aspect aspect, int amount) {
        if (!canAcceptAspect(aspect) || amount <= 0) {
            return 0;
        }
        int accepted = Math.min(amount, Math.max(0, CAPACITY - aspects.totalAmount()));
        if (accepted > 0) {
            aspects.add(aspect, accepted);
            setChangedAndSync();
        }
        return accepted;
    }

    public int removeEssentia(Aspect aspect, int amount) {
        int removed = aspects.removeUpTo(aspect, amount);
        if (removed > 0) {
            setChangedAndSync();
        }
        return removed;
    }

    public int originalSuctionAmount(Aspect aspect) {
        return amount() < CAPACITY ? ORIGINAL_RESERVOIR_SUCTION : EssentiaSuction.SOURCE_NONE;
    }

    /**
     * Original TileEssentiaReservoir.fillReservoir(): every five ticks the
     * reservoir actively pulls one unit from the transport on its configured
     * face when its suction (24) beats the neighbour suction and satisfies the
     * neighbour minimum suction.
     */
    private void fillFromFacingLikeTC4() {
        if (level == null || amount() >= CAPACITY) {
            return;
        }
        BlockEntity neighbour = level.getBlockEntity(worldPosition.relative(facing));
        Direction sideFromNeighbour = facing.getOpposite();
        EssentiaTubeBlockEntity.pullOneIntoReservoirLikeTC4(
                neighbour, sideFromNeighbour, ORIGINAL_RESERVOIR_SUCTION, this);
    }

    public int originalTickCounter() {
        return originalTickCounter;
    }

    public Aspect firstAspect() {
        return aspects.firstAspect();
    }

    public int amount() {
        return aspects.totalAmount();
    }

    public float fillRatio() {
        return Math.max(0.0F, Math.min(1.0F, amount() / (float) CAPACITY));
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
        tag.put(NBT_ASPECTS, aspects.save());
        tag.putByte(NBT_FACING, (byte) facing.get3DDataValue());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        aspects.clear();
        if (tag.contains(NBT_ASPECTS)) {
            aspects.load(tag.getCompound(NBT_ASPECTS));
            if (aspects.totalAmount() > CAPACITY) {
                aspects.clear();
            }
        }
        if (tag.contains(NBT_FACING)) {
            Direction loaded = Direction.from3DDataValue(tag.getByte(NBT_FACING));
            facing = loaded == null ? Direction.DOWN : loaded;
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
